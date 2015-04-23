package tools

import db.DBConnFactory
import entity.Event
import entity.Fight
import entity.Fighter
import entity.Organization
import org.jsoup.HttpStatusException
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements

import java.sql.Date
import java.util.concurrent.Callable
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantReadWriteLock
import java.util.regex.Matcher

/**
 * Created by justin on 7/15/2014.
 */
class FightScraper {
    static String _stub = "http://www.sherdog.com"
    static AtomicInteger counter = new AtomicInteger()

    static def getFight(def fs, String method, String ref, String round, String time, Event event) {
        def results = []
        List<Fighter> fighters = new ArrayList<>();
        [[fs[0][0], fs[0][1]], [fs[1][0], fs[1][1]]].each {
            def fighterUrl = it[0]
            def name = it[1]
            Fighter fighter = fighterMap.get(fighterUrl)
            if(fighter == null) {
                synchronized (fighterMap) {
                    if(!fighterMap.containsKey(fighterUrl)) {
                        fighter = new Fighter(name, fighterUrl)
                        fighterMap.put(fighterUrl, fighter)
                    }
                }
            }
            fighters.add(fighter)
        }
        if(fighters.findAll{it == null}.size() > 0) {
            throw new Exception("NULL FIGHTER! " + fs + "\nevent: " + event);
        }

        def winner = (fs[0][2] ==~ /win/) ? fighters[0] :
                     (fs[1][2] ==~ /win/) ? fighters[1] : null
        Integer roundInt = null
        try {
            roundInt = Integer.valueOf(round)
        } catch(Exception e) {
        }
        Integer timeInt = colonToSeconds(time)

        for(Fighter fighter : fighters) {
            synchronized (fighterMap.get(fighter.getUrl())) {
                if(!fighter.isInSync())
                    fighter.write()
            }
        }

        Fight f = new Fight(fighters[0].getUrl(), fighters[1].getUrl(), winner?.getUrl(), method, ref, roundInt, timeInt, event.getId())
        return f;
    }
    static int convertRoundTimeToSeconds(int round, String time) {
        return (round-1)*60 + colonToSeconds(time);
    }

    static def tryMultiple(String eventPage, int numTries) {
        Document doc = null
        Exception exp;
        while(numTries--) {
            try {
                doc = Jsoup.connect(eventPage).get();
            } catch(Exception e) {
                exp = e;
                continue;
            }
        }
        if(doc != null) {
            return doc
        } else {
            throw new ThreadDeath("Could not get page " + eventPage, exp);
        }
    }

    static def writeFightsForEvent(Event event) {
        String eventPage = _stub + event.getUrl();

        Document doc = tryMultiple(eventPage, 4);
        try {
            Elements l = doc.select('.fighter.left_side');
            Elements r = doc.select('.fighter.right_side');
            def headliners = [l, r].collect {
                Elements es = it
                String name = es.select('h3 span[itemprop=name]').text();
                String url = es.select('h3 a[href]').attr("href");
                String result = es.select('.final_result').text();
                [url, name, result]
            }
            if(l.size() == 0 || r.size() == 0)
                return;

            Elements b = doc.select('.footer .resume tr td')
            String matchNum = b.get(0).ownText().trim()
            String method = b.get(1).ownText()
            String ref = b.get(2).ownText()
            String round = b.get(3).ownText()
            String time = b.get(4).ownText()

            def fights = [(matchNum) : getFight(headliners, method, ref, round, time, event)]

            Elements remaining = doc.select('tr[itemprop=subEvent]')
            for (int i = 0; i < remaining.size(); i++) {
                Element fight = remaining.get(i);
                def fighters = ['.text_right', '.text_left'].collect {
                    String fighterUrl = fight.select("$it .fighter_result_data a[href]").attr("href");
                    String name = fight.select("$it .fighter_result_data span[itemprop=name]").text()
                    String result = fight.select("$it .fighter_result_data .final_result").text()
                    [fighterUrl, name, result]
                }
                if(fighters.collect{it[1]}.findAll{ it =~ /Unknown Fighter/}.size() > 0)
                    continue;
                Elements details = fight.select("td:not(td[class])")
                matchNum = details.get(0).ownText()
                method = details.get(1).ownText()
                ref = details.get(1).select("span").text()
                round = details.get(2).text()
                time = details.get(3).text()
                Fight f = getFight(fighters, method, ref, round, time, event)
                fights.put(matchNum, f)
            }

            for (Fight f : fights.values()) {
                f.write()
                int writeCount = counter.incrementAndGet()
                if(writeCount % 100 == 0) {
                    println "$writeCount fights written"
                }
            }
        } catch(Exception e) {
            println event;
            throw new Exception(e);
        }
    }




    static Integer colonToSeconds(String time) {
        def matcher = time =~ /(\d):(\d\d)/
        try {
            matcher[0]
        } catch(Exception e) {
            return null;
        }
        return Integer.valueOf(matcher[0][1])*60 + Integer.valueOf(matcher[0][2])
    }

    public static void main(String[] args) {
//        Event e = Event.get(54232)
//        writeFightsForEvent(e)
          getFightsForAllUnprocessedEvents()
    }

    static Map<String,Fighter> fighterMap = toMap(Fighter.getAll())

    static Map<String, Fighter> toMap(List<Fighter> fighters) {
        Map<String, Fighter> map = new ConcurrentHashMap<>();
        for(Fighter f : fighters) {
            map.put(f.url, f)
        }

        return map
    }

    public static Set<Integer> getProcessedEvents() {
        def conn = DBConnFactory.getInstance()
        def stmt = conn.prepareStatement("select distinct event_id from fights")
        def rs = stmt.executeQuery()
        Set<Integer> events = new HashSet<>()
        while(rs.next()) {
            events.add(rs.getInt(1))
        }

        return events
    }

    static int eventCount;
    public static void getFightsForAllUnprocessedEvents() {
        def pool = Executors.newFixedThreadPool(30)
        def eventIdSet = getProcessedEvents()
        def events = Event.getAll()
        eventCount = events.size() - eventIdSet.size()
        def results
        List<Future> futures
        try {
            futures = events.findAll{ !eventIdSet.contains(it.getId()) }.collect { e ->
                def event = (Event) e
                pool.submit({ -> writeFightsForEvent event } as Callable);
            }
            try {
                results = futures.collect { it.get() }
            }catch(Exception e) {
                throw e;
                System.exit(1)
            }
        } finally {
            pool.shutdown()
        }
        println "events, ${results.size()} written!"
    }
}
