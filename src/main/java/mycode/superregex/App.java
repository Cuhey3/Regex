/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mycode.superregex;

import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 *
 * @author nanashi
 */
public class App {

    public static void main(String[] args) throws IOException {
        String url = "http://ja.wikipedia.org/w/api.php?action=query&list=categorymembers&cmtitle=Category:%E6%97%A5%E6%9C%AC%E3%81%AE%E5%A5%B3%E6%80%A7%E5%A3%B0%E5%84%AA&cmlimit=500&format=xml&cmnamespace=0";
//        String url = "http://ja.wikipedia.org/w/api.php?action=query&list=categorymembers&cmtitle=Category:%E6%97%A5%E6%9C%AC%E3%81%AE%E7%94%B7%E6%80%A7%E5%A3%B0%E5%84%AA&cmlimit=500&format=xml&cmnamespace=0";
//        String url = "http://en.wikipedia.org/w/api.php?action=query&list=categorymembers&cmtitle=Category:Japanese_voice_actresses&cmlimit=500&format=xml&cmnamespace=0";
        Document doc = Jsoup.connect(url).get();
        StringBuilder sbb = new StringBuilder("(");
        while (true) {
            for (Element e : doc.select("categorymembers cm[title]")) {
                sbb.append(e.attr("title").replaceFirst(" \\(.+\\)$", "")).append("|");
            }
            if (doc.select("categorymembers[cmcontinue]").isEmpty()) {
                break;
            } else {
                System.out.println(url + "&cmcontinue=" + doc.select("categorymembers[cmcontinue]").get(0).attr("cmcontinue"));
                doc = Jsoup.connect(url + "&cmcontinue=" + doc.select("categorymembers[cmcontinue]").get(0).attr("cmcontinue")).get();
            }
        }
        String wao = new String(sbb).replaceFirst("\\|$", ")");
        Pattern p = Pattern.compile("\\([^\\[\\]\\(\\)]+\\|[^\\[\\]\\(\\)]+(?<!\\|)\\)");
        while (true) {
            int gap = 0;
            Matcher m = p.matcher(wao);
            StringBuilder sb = new StringBuilder(wao);
            while (m.find()) {
                String replace = getReplace(m.group(0));
                sb.replace(m.start() + gap, m.end() + gap, replace);
                gap += replace.length() - m.group().length();
            }
            wao = new String(sb);
            if (!p.matcher(wao).find()) {
                break;
            }
        }
        Pattern p0 = Pattern.compile("\\(([^\\[\\]\\(\\)\\|]*)\\)");
        wao = p0.matcher(wao).replaceAll("$1");
        doc = Jsoup.connect("http://ja.wikipedia.org/w/api.php?action=parse&page=%E6%B4%A5%E7%94%B0%E7%BE%8E%E6%B3%A2&prop=wikitext&format=xml").get();
        Pattern name = Pattern.compile(wao);
        Matcher m = name.matcher(doc.select("wikitext").text());
        HashMap<String,Integer> count = new HashMap<>();
        while(m.find()){
            Integer i = count.get(m.group());
            if(i == null){
                i = 0;
            }
            count.put(m.group(), ++i);
        }
        for(Map.Entry<String, Integer> s : count.entrySet()){
            System.out.println(s.getKey() + "\t" + s.getValue());
        }
        /*        doc = Jsoup.connect("http://www.koepota.jp/eventschedule/").get();
        
         System.out.println(name.pattern());
         LinkedHashMap<String, HashSet<String>> count = new LinkedHashMap<>();
         for (Element e : doc.select("td.number")) {
         Matcher m = name.matcher(e.text());
         while (m.find()) {
         String key = m.group();
         if (count.containsKey(key)) {
         HashSet<String> set = count.get(key);
         set.add(e.parent().select(".title").text());
         count.put(key, set);
         } else {
         HashSet<String> set = new HashSet<>();
         set.add(e.parent().select(".title").text());
         count.put(key, set);
         }
         }
         }
         for (Map.Entry<String, HashSet<String>> e : count.entrySet()) {
         System.out.println(e.getKey() + "\t" + e.getValue().size());
         }*/
    }

    static String getReplace(String match) {
//        System.out.print(match);
        match = match.replaceFirst("^\\(", "");
        match = match.replaceFirst("\\)$", "");
        TreeSet<String> ts = new TreeSet<>(new Comparator() {

            @Override
            public int compare(Object o1, Object o2) {
                int l1 = ((String) o1).length();
                int l2 = ((String) o2).length();
                if (l1 == l2) {
                    return ((String) o1).compareTo((String) o2);
                } else {
                    return l2 - l1;
                }
            }
        });
        ts.addAll(Arrays.asList(match.split("\\|")));
        TreeSet<Character> top = new TreeSet<>();
        for (String s : ts) {
            top.add(s.charAt(0));
        }
        if (top.size() == 1) {
            StringBuilder sb = new StringBuilder(top.first() + "(");
            for (String s : ts) {
                sb.append(s.substring(1)).append("|");
            }
//            System.out.println(" 2: => " + new String(sb).replaceFirst("\\|$", ")"));
            return new String(sb).replaceFirst("\\|$", ")");
        }
        TreeSet<Character> bottom = new TreeSet<>();
        for (String s : ts) {
            bottom.add(s.charAt(s.length() - 1));
        }
        if (bottom.size() == 1) {
            StringBuilder sb = new StringBuilder("(");
            for (String s : ts) {
                sb.append(s.substring(0, s.length() - 1)).append("|");
            }
//            System.out.println(" 3: => " + new String(sb).replaceFirst("\\|$", ")" + bottom.first()));
            return new String(sb).replaceFirst("\\|$", ")" + bottom.first());
        }
//        if (top.size() <= bottom.size()) {
        if (true) {
            StringBuilder sb = new StringBuilder("(");
            for (Character c : top) {
                sb.append(c).append("(");
                for (String s : ts) {
                    if (s.charAt(0) == c) {
                        sb.append(s.substring(1)).append("|");
                    }
                }
                sb = new StringBuilder(new String(sb).replaceFirst("\\|$", ")|"));
            }
//            System.out.println(" 4: => " + new String(sb).replaceFirst("\\|$", ")"));
            return new String(sb).replaceFirst("\\|$", ")");

        } else {
            StringBuilder sb = new StringBuilder("(");
            for (Character c : bottom) {
                sb.append("(");
                for (String s : ts) {
                    if (s.endsWith(c + "")) {
                        sb.append(s.substring(0, s.length() - 1)).append("|");
                    }
                }
                sb = new StringBuilder(new String(sb).replaceFirst("\\|$", ")")).append(c).append("|");
            }
//            System.out.println(" 5: => " + new String(sb).replaceFirst("\\|$", ")"));
            return new String(sb).replaceFirst("\\|$", ")");
        }
    }
}
