<fb:fbml version="1.1">
<?php include 'ameecalcstyles.php'; ?>
<fb:dashboard></fb:dashboard>
<div id="ameeapp">
<div class="fbbox">
<div class="boxHeader">Your carbon profile</div>
<div class="boxBottom">
<p id="carbonprofile"><span id="carbonprofilevalue"><?php echo number_format($ameecalc['carbonprofile']); ?></span><br />kgCO<sub>2</sub>/year</p>
<form style="padding-top: 8px;" action="index.php" method="post">
<input type="hidden" name="action" value="edit" />
<input type="submit" value="Change my usage values" />
</form>
</div>
</div>
</div>
</div>
</fb:fbml>
<?php include 'info.php'; ?>