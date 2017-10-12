package com.alfresco.aps.test.process;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.alfresco.aps.testutils.AbstractBpmnTest;
import com.alfresco.aps.testutils.ProcessInstanceAssert;

import org.activiti.engine.runtime.ProcessInstance;
import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:activiti.cfg.xml", "classpath:common-beans-and-mocks.xml" })
public class MessageCatchProcessUnitTest extends AbstractBpmnTest {
	
	static {
		appName = "Test App";
		processDefinitionKey = "MessageCatch";
	}

	@Test
	public void testProcessExecution() throws Exception {

		ProcessInstance processInstance = activitiRule.getRuntimeService()
				.startProcessInstanceByKey(processDefinitionKey);

		assertNotNull(processInstance);


		//Assert message and not execute
		unitTestHelpers.assertMessageWait(1, null, "message-catch", false, null);
		//Assert boundary message and not execute
		unitTestHelpers.assertMessageWait(1, null, "message-boundary", false, null);
		
		//Assert message and execute
		unitTestHelpers.assertMessageWait(1, null, "message-catch", true, null);
		//Assert boundary message and execute
		unitTestHelpers.assertMessageWait(1, null, "message-boundary", true, null);

		ProcessInstanceAssert.assertThat(processInstance).isComplete();
	}

}