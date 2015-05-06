import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.Semaphore;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Created by Антон on 04.05.2015.
 */
class ResultEvents implements Runnable
{
    static  TreeMap<String,Integer> map=new TreeMap<>();// коллекция слов и кол-ва их повторений
    static HashSet<String> list=new HashSet<>();//коллекция пересмотренных url - адресов
    static ArrayList<String> arrUrl=new ArrayList<>();// коллекция перебираемых URL -адресов
    static HashSet<String> listWord=new HashSet<>();//коллекция URL, где были найдены искомые слова
    Pattern pat;
    Matcher mat;
    String strUrl;
    Semaphore cb;
    PrintWriter pw;
    ResultEvents(String str,Semaphore bar)
    {
        cb=bar;
        this.pw=pw;
        pat= Pattern.compile("https?://(w{3})?.[a-z-0-9.]+.([a-z-0-9]+?.)?(ru|com|ua|php|org)");
        strUrl=str;
        new Thread(this);
    }
    public void searchUrl(String str) {
        try {
            URL url = new URL(str);
            String temp = null;
            boolean flag=false;
            URLConnection con = url.openConnection();
            BufferedReader buf = new BufferedReader(new InputStreamReader(con.getInputStream()));
            Stream<String> stream = buf.lines();
            Iterator<String> iter = stream.iterator();
            while (iter.hasNext()) {
                temp = iter.next();
                mat = pat.matcher(temp);
                while (mat.find()) {
                    flag=false;
                    list.add(mat.group());
                    for(String x:list)
                        if (x.equals(mat.group())) flag = true;
                    if(!flag) searchUrl(mat.group());
                }
            }
        } catch (MalformedURLException e) {
            e.getStackTrace();
        } catch (IOException e) {
            e.getStackTrace();
        }
    }
    public void searchWords()
    {
        for(String x:list)
        {
            try {
                URL url = new URL(x);
                URLConnection con=url.openConnection();
                BufferedReader buf=new BufferedReader(new InputStreamReader(con.getInputStream()));
                Stream<String> stream=buf.lines();
                Iterator<String> iter=stream.iterator();
                Pattern pattern=Pattern.compile("[, .:;<>-]");
                String[] str;
                while(iter.hasNext())
                {
                    str=pattern.split(iter.next());
                    for(String x1:str) {
                        for(Map.Entry<String,Integer> en: map.entrySet())
                            if (x1.equals(en.getKey()))
                            {
                                en.setValue(en.getValue()+1);
                                listWord.add(x);
                            }
                    }
                }
            }catch(MalformedURLException e)
            {
                e.getStackTrace();
            }catch (IOException e)
            {
                e.getStackTrace();
            }

        }
    }
    public void showUrl(PrintWriter p)
    {
        p.print("<tr>");
        p.print("<td>");
        p.print("<ol>");
        for(String s: list)
            p.print("<li>"+s+"</li>");
            p.print("</ol>");
            p.print("</td>");
            p.print("</tr>");
    }
    @Override
    public void run() {
        try {
            cb.acquire();
            searchUrl(strUrl);
            searchWords();
            cb.release();
        }catch(InterruptedException e)
        {
            e.getStackTrace();
        }
    }
}
@WebServlet(name = "Servlet",urlPatterns = "/")
public class Servlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
            response.setCharacterEncoding("UTF-8");
            request.setCharacterEncoding("UTF-8");
            String name=request.getParameter("name");
            String word=request.getParameter("word");
            String url=request.getParameter("url");
            ResultEvents.map.put(word, 0);
            ResultEvents.arrUrl.add(url);
            response.setContentType("text/html,charset=UTF-8");
            PrintWriter pw=response.getWriter();
            //создаем сессию
            HttpSession hs=request.getSession(true);
            pw.print("<html>");
            pw.print("<head>");
            pw.print("<title>Search_URL</title>");
            pw.print("<meta charset=\"UTF-8\">");
            pw.print("<style>");
            pw.print("div{");
            pw.print("width:700px;");
            pw.print("background: #b0e0e6;");
            pw.print("padding: 60px;");
            pw.print("border: solid 3px black;");
            pw.print("position: relative ;");
            pw.print("top: 50px;");
            pw.print("left:170px;}");
            pw.print("</style>");
            pw.print("</head>");
            pw.print("<body>");
            pw.print("<div>");
            pw.print("<table border=\"1\" width=\"700\">");
            pw.print("<tr>");
            pw.print("<tr>");
            pw.print("<td>");
            pw.print("<h3 color=\"red\" align=\"center\"> <a href=\"http://localhost:8080/index.jsp\">Перейти на главную страницу</a>");
            pw.print("</td>");
            pw.print("</tr>");
            pw.print("<tr>");
            pw.print("<td>");
            pw.print("<h3 color=\"darkblue\" align=\"center\">Входящие данные</h3>");
            pw.print("Пользователь: "+name+"<br>");
            pw.println("Слово поиска: " + word+"<br>");
            pw.println("URL-адрес поиска слова: " + url+"<br>");
        pw.print("</td>");
        pw.print("</tr>");
        Semaphore bar=new Semaphore(2);
        ResultEvents s=null;
        for(String x:ResultEvents.arrUrl) {
            s = new ResultEvents(x,bar);
            s.run();
        }
        s.showUrl(pw);
        pw.print("<tr>");
        pw.print("<td>");
        pw.println("<h3 color=\"darkblue\" align=\"center\">Результат поиска слова по дереву:<br>");
        for(Map.Entry<String,Integer> en:ResultEvents.map.entrySet())
            pw.format("%10s : %5d%n <br>", en.getKey(), en.getValue());
        pw.print("</td>");
        pw.print("</tr>");
        //отслеживание сеанса связи
        String user=(String)hs.getAttribute("user");
        if(user!=null)
        {
            pw.print("<tr>");
            pw.print("<td>");
            pw.print("<h3 color=\"darkblue\" align=\"center\">История запросов данного пользователя:");
            for(Map.Entry<String,Integer> en:ResultEvents.map.entrySet())
                pw.format("<br>%10s : %5d%n", en.getKey(), en.getValue());
            pw.print("</td>");
            pw.print("</tr>");
        }
        pw.print("</tr>");
            pw.print("</table>");
            pw.print("</div>");
            pw.print("</body>");
            pw.print("</html>");
        hs.setAttribute("user",name);
                pw.close();
    }
}
