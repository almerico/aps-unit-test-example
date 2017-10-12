package com.alfresco.aps.test.process;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.alfresco.aps.testutils.AbstractBpmnTest;
import com.alfresco.aps.testutils.assertions.ProcessInstanceAssert;

import org.activiti.engine.runtime.ProcessInstance;
import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:activiti.cfg.xml", "classpath:common-beans-and-mocks.xml" })
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

		ProcessInstanceAssert.assertThat(processInstance).signalWaitCountIs(1, "signal-catch")
				.executeSignalWaitAtivities("signal-catch", null).signalWaitCountIs(1, "signal-boundary")
				.executeSignalWaitAtivities("signal-boundary", null)
				.isComplete();

	}

}