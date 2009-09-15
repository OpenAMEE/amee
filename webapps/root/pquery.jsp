<!--
  copyright (c) 2009 Google inc.

  You are free to copy and use this sample.
  License can be found here: http://code.google.com/apis/ajaxsearch/faq/#license
-->

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
  <head>
    <meta http-equiv="content-type" content="text/html; charset=utf-8"/>
    <title>
      Google Visualization API Sample
    </title>
    <script type="text/javascript" src="http://www.google.com/jsapi"></script>
    <script type="text/javascript">
      google.load('visualization', '1', {packages: ['table']});
    </script>
    <script type="text/javascript">

    var isFirstTime = true;
    var options = {'showRowNumber': true};
    var data;
    var queryInput;

    var path = '<%= "/ptable" + request.getPathInfo() %>';
    var query = new google.visualization.Query(path);

    function sendAndDraw() {
      // Send the query with a callback function.
      query.send(handleQueryResponse);
    }

    function handleQueryResponse(response) {
      if (response.isError()) {
        alert('Error in query: ' + response.getMessage() + ' ' + response.getDetailedMessage());
        return;
      }
      data = response.getDataTable();
      var table = new google.visualization.Table(document.getElementById('querytable'));
      table.draw(data, {'showRowNumber': true});
      if (isFirstTime) {
      init();
      }
    }

    function setQuery(queryString) {
      // Query language examples configured with the UI
      query.setQuery(queryString);
      sendAndDraw();
      queryInput.value = queryString;
    }

    google.setOnLoadCallback(sendAndDraw);

    function init() {
      isFirstTime = false;
      queryInput = document.getElementById('display-query');
    }

    function setQueryFromUser() {
      setQuery(queryInput.value);
    }

    </script>
  </head>
<body style="font-family: Arial; border: 0 none;">
<table style='width: 100%;'>
  <tr>
    <td style="width: 50%; padding: 10px; vertical-align: top;">
    <div style='font-size: 15px; font-weight: bold; padding: 5px;'><input
      type="text" style="width: 100%" id='display-query' /> <br></br>
    <input type="button" value='edit &amp; submit' onclick="setQueryFromUser()" /></div>
    <div id="querytable"></div>
    </td>
  </tr>
</table>
</body>
</html>
