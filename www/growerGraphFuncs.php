<script language="Javascript">
pagename = "realtime";
function updateAllGraphs(id) {
  var i1 = document.graphForm.varGraph1.selectedIndex;
  updateGraph('imgGraph1', 0, id, i1, document.graphForm.varGraph1.options[i1].value);
  var i2 = document.graphForm.varGraph2.selectedIndex;
  updateGraph('imgGraph2', 1, id, i2, document.graphForm.varGraph2.options[i2].value);
  var i3 = document.graphForm.varGraph3.selectedIndex;
  updateGraph('imgGraph3', 2, id, i3, document.graphForm.varGraph3.options[i3].value);
  //if (isIE) billGatesSucks();
  billGatesSucks();
}

function billGatesSucks() {
  var r = Math.random();
  document['imgGraph1'].src += "&" + r;
  document['imgGraph2'].src += "&" + r;
  document['imgGraph3'].src += "&" + r;
  document['imgBar1'].src += "&" + r;
  document['imgBar2'].src += "&" + r;
  document['imgBar3'].src += "&" + r;
  document['imgBar4'].src += "&" + r;
}

function getGraphURL(name, id, index, ytitle) {
  if (name == "timeplots") {
    var ycol1 = new Array(26, 23, 5, 8, 17, 56, 35, 41, 38, 11, 23, 18, 20, 17, 35, 41, 38, 23, 47, 44, 44);
    var fac1 = new Array(1.8, 1.8, 1, 1, 1000, 0.3937, 1000, 1000, 1000, 1, 1, 1, 1, 1000, 1000, 1000, 1000, 1, 1, 1, 1);
    var file1 = new Array("daily", "daily", "daily", "daily", "NRel", "daily", "Nplant", "Nplant", "Nplant", "daily", "Nplant", "daily", "daily", "Pplant", "Pplant", "Pplant", "Pplant", "Pplant", "Nplant", "Nplant", "Pplant");

    var ycol2 = new Array(53, 47, 59, 35, 32, 30, 14, 29, 50, 32, 14, 32);
    var fac2 = new Array(0.3937, 78.74, 0.3937, 1, 0.3937, 0.3937, 1000, 0.3937, 0.3937, 1000000, 1000, 1000000);
    var file2 = new Array("daily", "daily", "daily", "daily", "daily", "daily", "Nleach", "daily", "daily", "Nleach", "Pleach", "Pleach");

    var ycol3 = new Array(20, 17, 23, 20, 17, 23);
    var fac3 = new Array(1000, 1000, 1000, 1000, 1000, 1000);
    var file3 = new Array("NRel", "NRel", "NRel", "PRel", "PRel", "PRel");

    var ycol = new Array(ycol1, ycol2, ycol3);
    var fac = new Array(fac1, fac2, fac3);
    var files = new Array(file1, file2, file3);

    var n = 0;
    while (index >= files[n].length) {
      index -= files[n].length;
      n++;
    }

    var path = "graph.php?file=" + pfix + id + "_" + files[n][index] + "stats.txt&ymin=0&xcol=0&ycol=";
    path += ycol[n][index];
    path += "&skipl=3&usedates=1&ytitle=" + ytitle + "&fac=";
    path += fac[n][index];
    if (n == 0 && index < 2) path += "&off=32";
    if (n == 1 && index == 1) path += "&ycol2=62&op=div";

    return path;
  } else if (name == "sumcharts") {
    var ycol1 = new Array(2, 3, 4, 5, 6, 7, 8, 8, 12, 14, 14, 17);
    var fac1 = new Array(0.142857, 0.2642, 0.2642, 0.2642, 0.2642, 0.2642, 1000, 10000, 0.1076, 1000, 10000, 1);

    var ycol2 = new Array(3, 4, 5, 6, 7, 8, 9, 9, 15);
    var fac2 = new Array(0.3937, 0.3937, 0.3937, 0.3937, 0.3937, 8.9, 8.9, 1, 1);

    var ycol = new Array(ycol1, ycol2);
    var fac = new Array(fac1, fac2);

    var n = 0;
    while (n < ycol.length && index >= ycol[n].length) {
      index -= ycol[n].length;
      n++;
    }

    if (n == 2) {
      if (index == 0) return document['imgBar3'].src;
      var path = "graph.php?file=" + pfix + id + "_summaryoutput.txt&ymin=0&yvals=";
      if (index == 1) {
        path += (document.graphForm.gFert.value * document.graphForm.gPctn.value / 100);
        path += ",8,13&keepval=0&skipl=3&ytitle=g/plant&xticks=N applied,Runoff N,Plant N uptake";
        path += "&xtickv=0.5,1.5,2.5&fac=1&style=3&summary=2";
      } else {
        path += (document.graphForm.gFert.value * document.graphForm.gPctP.value / 100);
        path += ",14,16&keepval=0&skipl=3&ytitle=g/plant&xticks=P applied,Runoff P,Plant P uptake";
        path += "&xtickv=0.5,1.5,2.5&fac=1&style=3&summary=2";
      }
      return path;
    }

    var path = "graph.php?file=" + pfix + id + "_";
    if (n == 1) {
      path += "summaryarea.txt";
    } else path += "summaryoutput.txt";
    if (pagename.indexOf("realtime") != -1) {
      path += "&ymin=0&yvals=" + ycol[n][index];
    } else {
      path += "&ymin=0&ycol=" + ycol[n][index];
    }
    path += "&skipl=3&ytitle=" + ytitle + "&fac=";
    if (n == 0 && index == 7) {
      fac[n][index] /= (document.graphForm.gFert.value * document.graphForm.gPctn.value);
    } else if (n == 0 && index == 10) {
      fac[n][index] /= (document.graphForm.gFert.value * document.graphForm.gPctP.value);
    }
    path += fac[n][index];
    if (pagename.indexOf("realtime") != -1) {
      path += "&xticks=" + ytitle + "&xtickv=1.5";
      path += "&style=3&summary=2";
    } else {
      path += "&style=3&summary=1";
    }
    return path;
  }
}

function updateGraph(imgName, n, id, index, ytitle) {
  var ycol1 = new Array(26, 23, 5, 8, 17, 56, 35, 41, 38, 11, 23, 18, 20, 17, 35, 41, 38, 23, 47, 44, 44);
  var fac1 = new Array(1.8, 1.8, 1, 1, 1000, 0.3937, 1000, 1000, 1000, 1, 1, 1, 1, 1000, 1000, 1000, 1000, 1, 1, 1, 1);
  var file1 = new Array("daily", "daily", "daily", "daily", "NRel", "daily", "Nplant", "Nplant", "Nplant", "daily", "Nplant", "daily", "daily", "Pplant", "Pplant", "Pplant", "Pplant", "Pplant", "Nplant", "Nplant", "Pplant");

  var ycol2 = new Array(53, 47, 59, 35, 32, 30, 14, 29, 50, 32, 14, 32);
  var fac2 = new Array(0.3937, 78.74, 0.3937, 1, 0.3937, 0.3937, 1000, 0.3937, 0.3937, 1000000, 1000, 1000000);
  var file2 = new Array("daily", "daily", "daily", "daily", "daily", "daily", "Nleach", "daily", "daily", "Nleach", "Pleach", "Pleach");

  var ycol3 = new Array(20, 17, 23, 20, 17, 23);
  var fac3 = new Array(1000, 1000, 1000, 1000, 1000, 1000);
  var file3 = new Array("NRel", "NRel", "NRel", "PRel", "PRel", "PRel");

  var ycol = new Array(ycol1, ycol2, ycol3);
  var fac = new Array(fac1, fac2, fac3);
  var files = new Array(file1, file2, file3);

  if (document.images) {
    if (document.graphForm.yearPlot.selectedIndex == 0) {
        //mean
        path = "graph.php?file=" + pfix + id + "_" + files[n][index] + "stats.txt&ymin=0&xcol=0&ycol=";
        path += ycol[n][index];
        path += "&skipl=3&usedates=1&ytitle=" + ytitle + "&fac=";
        path += fac[n][index];
        if (n == 0 && index < 2) path += "&off=32";
        if (n == 1 && index == 1) path += "&ycol2=62&op=div";
        document[imgName].src = path;
    } else {
        //year
        path = "yearlygraph.php?file=" + pfix + id + "_" + files[n][index] + "output.txt&ymin=0&xcol=1&ycol=";
        path += Math.floor(ycol[n][index]/3+2);
        path += "&year=" + document.graphForm.yearPlot.options[document.graphForm.yearPlot.selectedIndex].value;
        path += "&skipl=";
        if (files[n][index] == "NRel") path += "5"; else path += "2";
        path += "&usedates=1&ytitle=" + ytitle + "&fac=";
        path += fac[n][index];
        if (n == 0 && index < 2) path += "&off=32";
        if (n == 1 && index == 1) path += "&ycol2=22&op=div";
        document[imgName].src = path;
    }
  }
  //if (isIE) billGatesSucks();
  billGatesSucks();
}

function updateBar(imgName, n, id, index, ytitle) {

  var ycol1 = new Array(2, 3, 4, 5, 6, 7, 8, 8, 12, 14, 14, 17);
  var fac1 = new Array(0.142857, 0.2642, 0.2642, 0.2642, 0.2642, 0.2642, 1000, 10000, 0.1076, 1000, 10000, 1);

  var ycol2 = new Array(3, 4, 5, 6, 7, 8, 9, 9, 15);
  var fac2 = new Array(0.3937, 0.3937, 0.3937, 0.3937, 0.3937, 8.9, 8.9, 1, 1);

  var ycol3 = new Array();
  var fac3 = new Array();
  var ycol4 = new Array(8, 14);
  var fac4 = new Array(1, 1);

  var ycol = new Array(ycol1, ycol2, ycol3, ycol4);
  var fac = new Array(fac1, fac2, ycol3, ycol4);

  if (document.images) {
    path = "graph.php?file=" + pfix + id + "_";
    if (n == 1 && index < 7) {
      path += "summaryarea.txt";
    } else path += "summaryoutput.txt";
    path += "&ymin=0&ycol=" + ycol[n][index];
    path += "&skipl=3&ytitle=" + ytitle + "&fac=";
    if (n == 0 && index == 7) {
      fac[n][index] /= (document.graphForm.gFert.value * document.graphForm.gPctn.value);
    } else if (n == 0 && index == 10) {
      fac[n][index] /= (document.graphForm.gFert.value * document.graphForm.gPctP.value);
    }
    path += fac[n][index];
    path += "&style=3&summary=1";
    if (n == 3) {
      path = "graph.php?file=" + pfix + id + "_summaryoutput.txt&ymin=0&yvals=";
      if (index == 0) {
	path += (document.graphForm.gFert.value * document.graphForm.gPctn.value / 100);
	path += ",8,13&keepval=0&skipl=3&ytitle=g/plant&xticks=N applied,Runoff N,Plant N uptake";
	path += "&xtickv=0.5,1.5,2.5&fac=1&style=3&summary=2";
      } else {
        path += (document.graphForm.gFert.value * document.graphForm.gPctP.value / 100);
        path += ",14,16&keepval=0&skipl=3&ytitle=g/plant&xticks=P applied,Runoff P,Plant P uptake";
        path += "&xtickv=0.5,1.5,2.5&fac=1&style=3&summary=2";
      }
    }
    document[imgName].src = path;
  }
  //if (isIE) billGatesSucks();
  billGatesSucks();
}

function updateBarRT(imgName, n, id, index, ytitle) {
  
  var ycol1 = new Array(2, 3, 4, 5, 6, 7, 8, 8, 12, 14, 14, 17);
  var fac1 = new Array(0.142857, 0.2642, 0.2642, 0.2642, 0.2642, 0.2642, 1000, 10000, 0.1076, 1000, 10000, 1);

  var ycol2 = new Array(3, 4, 5, 6, 7, 8, 9, 9, 15);
  var fac2 = new Array(0.3937, 0.3937, 0.3937, 0.3937, 0.3937, 8.9, 8.9, 1, 1);

  var ycol3 = new Array(8);
  var fac3 = new Array(1000);

  var ycol = new Array(ycol1, ycol2, ycol3);
  var fac = new Array(fac1, fac2, fac3);

  if (document.images) {
    path = "graph.php?file=" + pfix + id + "_";
    if (n == 1) {
      path += "summaryarea.txt";
    } else path += "summaryoutput.txt";
    path += "&ymin=0&yvals=" + ycol[n][index];
    path += "&skipl=3&ytitle=" + ytitle + "&fac=";
    if (n == 0 && index == 7) {
      fac[n][index] /= (document.graphForm.gFert.value * document.graphForm.gPctn.value);
    } else if (n == 0 && index == 10) {
      fac[n][index] /= (document.graphForm.gFert.value * document.graphForm.gPctP.value);
    }
    path += fac[n][index];
    path += "&xticks=" + ytitle + "&xtickv=1.5";
    path += "&style=3&summary=2";
    document[imgName].src = path; 
  }
  //if (isIE) billGatesSucks();
  billGatesSucks();
}

</script>
