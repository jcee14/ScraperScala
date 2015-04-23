package tools

import entity.Event
import entity.Organization
import org.jsoup.HttpStatusException
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.Future

import java.sql.Date
import java.util.concurrent.atomic.AtomicInteger

/**
 * Created by justin on 7/15/2014.
 */
class EventScraper {
    static String _seed = "http://www.sherdog.com/organizations/King-of-the-Cage-1"
    static String eventLocationSelect = '#featured_upcoming_tab table tbody tr td[itemprop]';
    static String eventNameSelect = "#featured_upcoming_tab table tbody tr td a span"
    static String eventUrlSelect = "#featured_upcoming_tab table tbody tr td a"
    static String eventDateSelect = '#featured_upcoming_tab table tbody tr td meta'
    static def selectors = [ eventLocationSelect, eventNameSelect, eventUrlSelect, eventDateSelect ]

    static def getters =
        [
            "location" : { d ->
                Document doc = (Document) d
                def elements = doc.select(eventLocationSelect)
                return elements.collect{ it.text() }
            },
            "name" : { d ->
                Document doc = (Document) d
                def elements = doc.select(eventNameSelect)
                return elements.collect{ it.text() }
            },
            "url" : { d ->
                Document doc = (Document) d
                def elements = doc.select(eventUrlSelect)
                return elements.collect{ it.attr("href") }
            },
            "date" : { d ->
                Document doc = (Document) d
                def elements = doc.select(eventDateSelect)
                return elements.collect{
                    try {
                        Date.valueOf(it.attr("content").substring(0, 10))
                    } catch(IllegalArgumentException e) {
                        Date.valueOf("1900-01-01")
                    }
                }
            },
        ]

    static AtomicInteger counter = new AtomicInteger(0)

    static def writeEventsForOrgId(int id) {
        String seed = _seed;
        def pieces = seed.split('-')
        pieces[-1] = id
        seed = pieces.join('-')
        Document doc
        try {
            doc = Jsoup.connect(seed).get();
        } catch (HttpStatusException e) {
        } catch (Exception e) {
            doc = Jsoup.connect(seed).get();
        }

        if(doc != null) {
            def eventValues = getters.collectEntries { field, getter ->
                [ (field), getter(doc) ]
            }
            def events = []
            for(int i = 0; i < eventValues.get(eventValues.keySet()[0]).size(); i++) {
                //new Event(eventValues.get("location")[i], eventValues.get("location")[i], id, )
                Event e = new Event()
                eventValues.keySet().each {
                    e[it] = eventValues.get(it)[i]
                    e["orgId"] = id
                }
                if(e.date.before(Date.valueOf(Instant.now()))) {
                    events << e
                }

            }
            events.each {
                Event e = it
                //e.write()
                e.println()
                def writeCount = counter.incrementAndGet()
                if(writeCount % 1000 == 0) {
                    println writeCount
                }
                return e.getUrl()
            }
        } else {
            println "event has no data!"
        }
        return null;
    }

    public static void getAllEventsForAllOrgs() {
        def pool = Executors.newFixedThreadPool(40)
        def results
        List<Future> futures
        try {
            futures = Organization.getAll().collect { o ->
                def org = (Organization) o
                pool.submit({ -> writeEventsForOrgId o.getId() } as Callable);
            }
            results = futures.collect { it.get() }
        } finally {
            pool.shutdown()
        }
        println "orgIds, ${results.size()} written!"
    }

    public static void main(String[] args) {
        getAllEventsForAllOrgs()
    }

    public static String getUrlEnding(String url) {
        def match = url =~ /.*\/(.*)/
        return match[0][1]
    }
/*
    private Fight getFight(Element fightLine) {
        Fight fight = new Fight();
        Elements cols = fightLine.select("td")
        String result = cols.get(0).text()
        String vs = FighterPage.getUrlEnding(cols.get(1).select("a[href]").get(0).attr("href"))
        String event = FighterPage.getUrlEnding(cols.get(2).select("a[href]").get(0).attr("href"))
        String date = cols.get(2).select(".sub_line").text().replace(' ', '')
        String method = cols.get(3).ownText()
        String referee = cols.get(3).select(".sub_line").text()
        String round = cols.get(4).text()
        String time = cols.get(5).text()

        fight.winner_id = (result.trim().toLowerCase() ==~ "win") ? fighterId : vs
        fight.loser_id = (result.trim().toLowerCase() ==~ "win") ? vs : fighterId
        fight.event = event
        fight.date = date
        fight.method = method
        fight.referee = referee
        fight.endRound = round
        fight.endTime = time

        return fight
    }
    */
}
