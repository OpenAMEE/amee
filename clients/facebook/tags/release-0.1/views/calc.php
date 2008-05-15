<fb:fbml version="1.1">
<?php //http://wiki.developers.facebook.com/index.php/Validation_Examples ?>
<script type="text/javascript">
function checkForm(form) {
	var params = form.serialize();
	if (parseInt(params.gas, 10) <= 0 and parseInt(params.elec, 10) <= 0 and parseInt(params.car, 10) <= 0 and parseInt(params.fly, 10) <= 0) {
		alert("Oh come on, you can do better than that!");
		return true;
	} else {
		return false;
	}
}
</script>
<?php include 'ameecalcstyles.php';?>
<fb:dashboard></fb:dashboard>
<div id="ameeapp">
<div class="fbbox">
<div class="boxHeader">Calculate your carbon profile</div>
<div class="boxBottom">
<form id="calcform" action="index.php" method="post" onsubmit="return checkForm(this);">
<div class="question">
<label for="country">What country are you in?</label>
<select name="ameecalc[country]" id="country">
<?php
foreach($iso3166 as $countrycode => $countryname) {
	echo "\t<option label=\"$countryname\" value=\"$countrycode\"";
	if($ameecalc['country'] == $countrycode) {
		echo ' selected="selected"';
	}
	echo ">$countryname</option>\n";
}
?>
</select>
<div class="question">
<label for="gas">How much gas (not petrol!) do you use per year?</label>
<input name="ameecalc[gas]" id="gas" type="text" size="5" maxlength="6" value="<?php echo $ameecalc['gas']; ?>" />kWh
</div>
<div class="question">
<label for="elec">How much electricity do you use per year</label>
<input name="ameecalc[elec]" id="elec" type="text" size="5" maxlength="6" value="<?php echo $ameecalc['elec']; ?>" />kWh
</div>
<div class="question">
<label for="car">About how far do you drive each year?</label>
<input name="ameecalc[car]" id="car" type="text" size="5" maxlength="6" value="<?php echo $ameecalc['car']; ?>" />,000 miles
</div>
<div class="question">
<label for="fly">About how many flights do you take per year?</label>
<input name="ameecalc[fly]" id="fly" type="text" size="5" maxlength="6" value="<?php echo $ameecalc['fly']; ?>" />
</div>
<div class="question">
<input type="hidden" name="action" value="calc" />
<input id="calc" type="submit" value="Calculate my profile" />
</div>
</div>
</form>
</div>
</div>
</fb:fbml>
<?php include 'info.php'; ?>