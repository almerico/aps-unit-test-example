package com.alfresco.aps.test.dmn;

import static com.alfresco.aps.testutils.TestUtilsConstants.DMN_RESOURCE_PATH;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.activiti.dmn.engine.RuleEngineExecutionResult;
import com.activiti.dmn.engine.domain.entity.DmnDeployment;
import com.alfresco.aps.testutils.AbstractDmnTest;
import com.alfresco.aps.testutils.resources.ActivitiResources;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:activiti.dmn.cfg.xml" })
public class DmnUnitTest extends AbstractDmnTest {
	
	String decisonTableKey = "dmntest";
	String appName = "Test App";
	
	@Before
	public void before() throws Exception {
		
		if(env.getProperty ("aps.app.download", Boolean.class, false)){
			
			ActivitiResources.forceGet(appName);
		}
		
		//Deploy the dmn files
		Iterator<File> it = FileUtils.iterateFiles(new File(DMN_RESOURCE_PATH), null, false);
		while (it.hasNext()) {
			String bpmnXml = ((File) it.next()).getPath();

			String extension = FilenameUtils.getExtension(bpmnXml);
			if (extension.equals("dmn")) {
				DmnDeployment dmnDeplyment = repositoryService.createDeployment()
						.addInputStream(bpmnXml, new FileInputStream(bpmnXml)).deploy();
				deploymentList.add(dmnDeplyment.getId());
			}
		}
	}

	@After
	public void after() {
		for (Long deploymentId : deploymentList) {
			repositoryService.deleteDeployment(deploymentId);
		}
		deploymentList.clear();
	}

	@Test
	public void testDMNExecution() throws Exception {
		Map<String, Object> processVariablesInput = new HashMap<>();
		processVariablesInput.put("input", "xyz");
		RuleEngineExecutionResult result = ruleService.executeDecisionByKey(decisonTableKey, processVariablesInput);
		Assert.assertNotNull(result);
		Assert.assertEquals(1, result.getResultVariables().size());
		Assert.assertSame(result.getResultVariables().get("output").getClass(), String.class);
		Assert.assertEquals(result.getResultVariables().get("output"), "abc");
	}
	
	@Test
	public void testDMNExecutionNoMatch() throws Exception {
		Map<String, Object> processVariablesInput = new HashMap<>();
		processVariablesInput.put("input", "dfdsf");
		RuleEngineExecutionResult result = ruleService.executeDecisionByKey(decisonTableKey, processVariablesInput);
		Assert.assertEquals(0, result.getResultVariables().size());
	}

}