<%--
  Created by IntelliJ IDEA.
  User: Антон
  Date: 04.05.2015
  Time: 11:24
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
  <head>
    <title>Search_URL</title>
    <meta charset="UTF-8">
    <style>
      div.main{
        width:700px;
        background: #00fa9a;
        padding: 60px;
        border: solid 3px black;
        position: relative ;
        top: 50px;
        left:170px;
        background-image: url("E:\Java_Project\Search_URL_Servlet\web\1.gif");
      }
      input{
        border:solid 2px gray;
        height: 30px;
        font-size: medium;
      }
      p{
        padding-left: 30px;
        font-size: 1.5em;
        color:darkblue;
      }
      p.submit{
        padding-left: 70px;
      }
    </style>
  </head>
  <body>
   <div class="main">
     <form name="formal" action="http://localhost:8080/Search_URL_Servlet/src/Servlet/" method="post">
       <table border="1" width="700" cellpadding="20">
         <tr>
           <td align="center" bgcolor="#deb887">
           <p style="color:mediumblue"> <B>"Поиск слов по URL - адресам."</B></p>
           </td>
         </tr>
       </table>
       <p> Имя пользователя <input type="text" placeholder="Введите свое имя" name="name">
       <p> Слово <input type="text" placeholder="Введите слово" name="word">
       <p> URL <input type="text" placeholder="Введите URL" name="url">
       <p class="submit"><input style="width: 350px" type ="submit" value="Поиск" >
     </form>
   </div>
  </body>
</html>
