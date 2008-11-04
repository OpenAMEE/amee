<#include '/includes/before_content.ftl'>
<h1>Data Upload</h1>
<p>
<#assign p0 = environmentPIG.rootPathItem>
<ul>
	<#list p0.children as p1>
		<#if p1.objectType == 'DC'>
			<#if !p1.childrenAvailable>
				<form action='/data/upload?method=put&categoryUid=${p1.uid}' method='POST' enctype='multipart/form-data'>
				<li>${p1.path} <input type='file' name='carbonDataFile'/><input type='submit' value="Upload"/></li>
				</form>
			<#else>
				<li>${p1.path}</li>
			</#if>
			<ul>
				<#list p1.children as p2>
					<#if p2.objectType == 'DC'>
					<#if !p2.childrenAvailable>
						<form action='/data/upload?method=put&categoryUid=${p2.uid}' method='POST' enctype='multipart/form-data'>
						<li>${p2.path} <input type='file' name='carbonDataFile'/><input type='submit' value="Upload"/></li>
						</form>
					<#else>
						<li>${p2.path}</li>
					</#if>
						<ul>
							<#list p2.children as p3>
								<#if p3.objectType == 'DC'>
								<#if !p3.childrenAvailable>
									<form action='/data/upload?method=put&categoryUid=${p3.uid}' method='POST' enctype='multipart/form-data'>
									<li>${p3.path} <input type='file' name='carbonDataFile'/><input type='submit' value="Upload"/></li>
									</form>
								<#else>
									<li>${p3.path}</li>
								</#if>
									<ul>
										<#list p3.children as p4>
											<#if p4.objectType == 'DC'>
											<#if !p4.childrenAvailable>
												<form action='/data/upload?method=put&categoryUid=${p4.uid}' method='POST' enctype='multipart/form-data'>
												<li>${p4.path} <input type='file' name='carbonDataFile'/><input type='submit' value="Upload"/></li>
												</form>
											<#else>
												<li>${p4.path}</li>
											</#if>
												<ul>
													<#list p4.children as p5>
														<#if p5.objectType == 'DC'>
														<#if !p5.childrenAvailable>
															<form action='/data/upload?method=put&categoryUid=${p5.uid}' method='POST' enctype='multipart/form-data'>
															<li>${p5.path} <input type='file' name='carbonDataFile'/><input type='submit' value="Upload"/></li>
															</form>
														<#else>
															<li>${p5.path}</li>
														</#if>
															<ul>
																<#list p5.children as p6>
																	<#if p6.objectType == 'DC'>
																	<#if !p6.childrenAvailable>
																		<form action='/data/upload?method=put&categoryUid=${p6.uid}' method='POST' enctype='multipart/form-data'>
																		<li>${p6.path} <input type='file' name='carbonDataFile'/><input type='submit' value="Upload"/></li>
																		</form>
																	<#else>
																		<li>${p6.path}</li>
																	</#if>
																	</#if>
																</#list>
															</ul>
														</#if>
													</#list>
												</ul>
											</#if>
										</#list>
									</ul>
								</#if>
							</#list>
						</ul>
					</#if>
				</#list>
			</ul>
		</#if>
	</#list>
</ul>
</p>
<#include '/includes/after_content.ftl'>