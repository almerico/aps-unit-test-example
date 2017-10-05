package com.alfresco.aps.test.dmn;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.activiti.dmn.engine.RuleEngineExecutionResult;
import com.alfresco.aps.testutils.AbstractDmnTest;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:activiti.dmn.cfg.xml" })
public class DmnUnitTest extends AbstractDmnTest {
	
	static {
		appName = "Test App";
		decisonTableKey = "dmntest";
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