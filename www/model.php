<?php session_start(); ?>
<html>
<head>
<title>Container Crop Management Tool - Model Concepts</title>
<?php require('header.php'); ?> 
<h1 class="h1-container">CCROP – Model Concepts*<font color="#ffffff" class="h1-shadow">CCROP – Model Concepts*</font></h1>
*See <a href="source/UserManual.pdf" target="ccmt">CCROP User Manual</a> for detail on input parameters needed, processes and functions simulated, and output generated.
<ul>
<li><span class="orange">What is a plant growth model?</span><br>
A plant growth model is a computer program which uses mathematical equations to describe how plants grow. Fortunately, models which simulate crop growth and development have been successfully developed for many of the world’s important crops. However, very little effort has been made to model ornamental crop production in containers – until now.
<br><br>
<li><span class="orange">What is CCROP?</span><br>
The plant growth model we have developed for simulating production of ornamental plants in small containers is called <b>CCROP</b> which is an acronym for
<b>Container Crop Resource Optimization Program</b>.
<br><br>
<b><u>Reference:</u></b>
<br>
<span class="redhead">
Million, J.B., Ritchie, J.T., Yeager, T.H., Larsen, CA, Warner, CD, and J.P. Albano. 2011. CCROP – Simulation model for container-grown nursery plant production. Scientia Horticulturae 130(4):874-886.
</span>
<br><br>
<li><span class="orange">How does CCROP approach simulating plant growth?</span><br>
The foundation of CCROP
is the simulation of <B>leaf area</B> growth and development. The
rate of leaf area development, i.e. leaf appearance, is controlled
primarily by temperature. The leaf canopy controls the amount of
light intercepted for <B>photosynthesis</B> thereby controlling the
amount of energy available for additional leaf area growth and
biomass accumulation. Leaf area also influences <B>evapotranspiration
(ET) </B>rates with soil evaporation rates decreasing and plant
transpiration rates increasing as the leaf canopy approaches
capturing 100% of incoming solar radiation. Through its affect on ET,
photosynthesis, and plant growth, simulation of leaf area growth
therefore provides a means for determining plant water and nutrient
demands. If daily water and nutrient demands of the plant exceed
daily water and nutrient supplies in the substrate, plant growth is
reduced through negative feedback.
<br><br>
<li><span class="orange">What assumptions are made for CCROP?</span><br>
<ol>
<li>All plants are the same. We know that this is not true but simulations are based upon an average, idealized plant.
<li>Except for water-holding properties, substrates are assumed to behave identically with regards to chemical and physical properties.
<li>There are no pest or disease problems affecting growth.
<li>Irrigation water is applied uniformly and the amount applied is what would be caught at the top level of the plant canopy
<li>Controlled-release fertilizers (CRF) are assumed to have similar release patterns. In other words, differences in N release from CRFs under a given set of growing conditions are due to differences in the longevity rating of the CRFs.
<li>Transplants are uniform and of good quality.
<li>Plants are assumed to have been watered thoroughly at planting so that the substrate is at container capacity at the start.
<li>Substrate is fertilized at planting.
<li>Plants are grown on industry-standard, black, woven, polypropylene groundcloth.
<li>Container spacing is uniform throughout the production period.
</ol>
<br><br>
<li><span class="orange">What language is CCROP written in? Can I view the code?
</span><br>
CCROP was developed and written in FORTRAN computer language.  Please contact Jeff Million or Tom Yeager for obtaining access to the FORTRAN code.
<br><br>
<li><span class="orange">What are Container Crop Management Tools?</span><br>
Container Crop Tools provide a user-friendly means for changing plant, weather and management input files, running CCROP, and viewing the output (see figure below).
<img src="images/model_diagram.png" align="center" alt="model diagram">
<br><br>
<li><span class="orange">What tools are available?</span><br>
<ol>
(1) <a href="driver_grower.php">GROWER TOOL</a>: This tool is streamlined for evaluating practices which have a major impact on profitability and resource conservation. The GROWER TOOL runs a simulation based on one set of input conditions. The output is more detailed and includes daily time-plots as well as summary plots and tables.
<br><br>
(2) <a href="comparisons.php">COMPARISON TOOLS</a>: This set of tools allows the user to run 'what-if' experiments to evaluate several levels of a given production practice. Simulations are run to compare two or more levels of a given factor or practice. For example, the user can evaluate the effect of four different fertilizer rates or the effect of three different irrigation schedules. For these comparisons, all other production practices selected by the user are held constant. The output includes comparison summary plots and tables (no daily time-plots).
<br><br>
(3) <a href="driver_tech.php">TECHNICAL TOOL</a>: This tool allows the user to change any input parameter in the CCROP model.
<br><br>
(4) <a href="realtime.php">REAL-TIME IRRIGATION TOOL</a>: This irrigation scheduling tool provides the user with a daily recommended amount of irrigation water to apply based upon simulated day-to-day progress of a crop.
</ol>
<br><br>
<li><span class="orange">Does it work with all plant species?</span><br>
CCROP is based on research with two plant species:
<br>
(1) <i>Viburnum odoratissimum</i> (sweet viburnum) is a fast-growing woody ornamental with large leaves and an upright-spreading growth habit
<br>
(2) <i>Ilex vomitoria</i> (dwarf yaupon holly) is a slow-growing woody ornamental with small leaves and a broad-spreading growth habit. 
<br>
<table border=0 width=100% align="center">
<tr><td><img src="images/vibur.png" width=360><br><span class="redhead"><i>Viburnum odoratissimum</i> (sweet viburnum)</span></td>
<td><img src="images/ilex.png" width=360><br><span class="redhead"><i>Ilex vomitoria</i> (dwarf yaupon holly)</span></td>
</tr></table>
<br><br>
<li><span class="orange">Does CCROP work with all container sizes?</span><br>
CCROP simulates production in trade #1 or trade #3 containers watered with overhead, sprinkler irrigation.
<br><br>
<li><span class="orange">What are the default inputs?</span><br>
Default values are included for all management inputs. These values are not necessarily recommended but are provided as a means for running the program with minimal input changes. The user can change one or more of the default settings and run the simulation. While all input management variables in CCROP are accessible in the <a href="driver_tech.php">TECHNICAL TOOL</a>, many management input variables are not accessible or are offered with limited options in the <a href="driver_grower.php">GROWER TOOL</a>, <a href="comparisons.php">COMPARISON TOOLS</a>, and <a href="realtime.php">REAL-TIME IRRIGATION SCHEDULING TOOL</a>.
<br><br>
<u>Input variables that can <b>not</b> be modified or have limited options include:</u>
<br>
<ol>
<li>Maximum number of container moves is limited to one for trade #1 containers and two for trade #3 containers.
<li>Liner transplant has a leaf area of 150 cm<sup>2</sup>, height of 14 cm, width 12 cm, N concentration of 0.8% in shoots and 0.6% in roots.
<li>Container size options limited to:
<ol type="a" style="margin-top: 0px;">
  <li>Trade #1 (2400 cm<sup>3</sup> of substrate and top diameter of 16 cm)
  <li>Trade #3 (10000 cm<sup>3</sup> of substrate and top diameter of 28 cm)
</ol>
<li>Substrate available water-holding capacity is assumed to be 25% by volume (50% drained upper limit and 25% lower limit). This is equivalent to 600 cm<sup>3</sup> per trade #1 container (1.2 inches) or 2500 cm<sup>3</sup> (4.1 inch) per trade #3.
<li>When model-recommended irrigation rate is selected, the managed allowable deficit (MAD) is 10% (percent of available water that is lost before irrigation is triggered).
<li>Nitrogen and phosphorus concentration of irrigation water is 0 mg/L (ppm).
<li>No supplemental fertilizer applications can be made.
<li>Automatic pruning is assumed (pruning will be delayed if growing conditions are less than optimal):
<ol type="a" style="margin-top: 0px;">
  <li>Trade #1 pruned once: 8 weeks after planting (WAP)
  <li>Trade #3 pruned three times: 8 WAP, 14 WAP, and 20 WAP
</ol>
</ol>
<br>
Note:  Use the <a href="driver_tech.php">TECHNICAL TOOL</a> to have full control of all input variables.
<br><br>
<li><span class="orange">What is the difference between output on a container-basis versus an area-basis?</span><br>
CCROP simulates plant growth as well as water and nutrient dynamics during production. This means there are a lot of output variables to consider!  Units must also be considered.  In this regard, CCROP outputs are expressed on either a container basis (often useful for resource efficiency evaluations) or an area basis (often useful for environmental evaluations). Conclusions drawn may be different using the two types of output because container density (number of containers per area) can change if containers are moved during the season.
The <a href="driver_tech.php">TECHNICAL TOOL</a> uses metric units for all input and output variables while a combination of English and metric units are used in <a href="driver_grower.php">GROWER TOOL</a>, <a href="comparisons.php">COMPARISON TOOLS</a>, and <a href="realtime.php">REAL-TIME IRRIGATION SCHEDULING TOOL</a>.
<br><br>
<!--
<li><span class="orange">What if I want more information about CCROP?</span><br>
* Please see <a href="source/UserManual.pdf" target="ccmt">CCROP User Manual</a> for detailed information on all aspects of CCROP. Additional comments and links can also be found in the tools.
-->
</ul>
<?php require('footer.php'); ?>
