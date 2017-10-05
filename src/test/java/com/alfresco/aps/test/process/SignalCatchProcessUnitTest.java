package com.alfresco.aps.test.process;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.alfresco.aps.testutils.AbstractBpmnTest;
import org.activiti.engine.runtime.ProcessInstance;
import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:activiti.cfg.xml", "classpath:common-beans-and-mocks.xml" })
@TestPropertySource(value="classpath:local-dev-test.properties")
public class SignalCatchProcessUnitTest extends AbstractBpmnTest {
	
	static {
		appName = "Test App";
		processDefinitionKey = "SignalCatch";
	}

	@Test
	public void testProcessExecution() throws Exception {

		ProcessInstance processInstance = activitiRule.getRuntimeService()
				.startProcessInstanceByKey(processDefinitionKey);

		assertNotNull(processInstance);


		//Assert signal and not execute
		unitTestHelpers.assertSignalWait(1, null, "signal-catch", false, null);
		//Assert boundary signal and not execute
		unitTestHelpers.assertSignalWait(1, null, "signal-boundary", false, null);
		
		//Assert signal and execute
		unitTestHelpers.assertSignalWait(1, null, "signal-catch", true, null);
		//Assert boundary signal and execute
		unitTestHelpers.assertSignalWait(1, null, "signal-boundary", true, null);

		unitTestHelpers.assertNullProcessInstance(processInstance.getProcessInstanceId());
	}

}