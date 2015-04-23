package tools

import entity.Organization
import org.jsoup.HttpStatusException
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements

import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.Future

/**
 * Created by justin on 7/15/2014.
 */
class OrgScraper {
    static String _seed = "http://www.sherdog.com/organizations/King-of-the-Cage-1"
    static String orgNameSelect = "body div.container div.content div.col_left div.module_header h2";

    static def getOrg(int id) {
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
            Elements results = doc.select(orgNameSelect)
            String orgName = results.text()
            println "$id - $orgName"
            Organization org = new Organization(id, orgName)
            return org
            //orgs.add(org)
        } else {
            println "orgId $id has no data!"
        }
        return null;
    }

    public static void findOrgs() {
//        def orgIdMap = Organization.getAll().collectEntries { org ->
//            [(org.getId()) : true ]
//        }
        def orgIdMap = [:]
        List<Organization> orgs = Collections.synchronizedList(new ArrayList<>());
        def pool = Executors.newFixedThreadPool(40)
        def defer = { c -> pool.execute(c as Runnable) }
        def lastId = orgIdMap.isEmpty() ? 0 : orgIdMap.keySet().sort()[-1] + 1
        def ids = (lastId..9000).findAll{ !orgIdMap.containsKey(it) }
        def results
        List<Future> futures

        try {
            futures = ids.collect { id ->
                pool.submit({ -> getOrg id } as Callable);
            }

            results = (futures.collect { it.get() }).findAll { it != null }
        } finally {
             pool.shutdown()
        }
        println "${results.size()} orgs found!"
        try {
            pool = Executors.newFixedThreadPool(30)
            futures = results.collect {
                def org = (Organization) it
                println org
                //pool.submit({ -> org.write(); println "${org.getId()} written!" } as Callable);
            }
            futures.each { it.get() }
        } finally {
            pool.shutdown()
        }
        println "all done!"
    }

    public static void getCurrentOrganizations() {

    }

    public static void main(String[] args) {
        findOrgs();
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
