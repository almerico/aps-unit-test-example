package com.alfresco.aps.test.process;

import java.util.HashMap;
import org.activiti.engine.delegate.DelegateExecution;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.alfresco.aps.testutils.AbstractBpmnTest;
import com.alfresco.aps.testutils.ProcessInstanceAssert;

import org.activiti.engine.runtime.ProcessInstance;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:activiti.cfg.xml", "classpath:common-beans-and-mocks.xml" })
public class APSRestStepProcessUnitTest extends AbstractBpmnTest {
	
	static {
		appName = "Test App";
		processDefinitionKey = "APSRestStepProcess";
	}
	
	@Before
	public void beforeTest() throws Exception {
		
		doAnswer(new Answer<Void>() {
			@Override
			public Void answer(InvocationOnMock invocation) throws Throwable {
				
				Object[] arg = invocation.getArguments();
				DelegateExecution execution = (DelegateExecution) arg[0];
				HashMap<String, String> fieldExtensions = new HashMap<String, String>();
				fieldExtensions.put("restUrl", "https://api.github.com/");
				fieldExtensions.put("httpMethod", "GET");
				unitTestHelpers.assertFieldExtensions(2, execution, fieldExtensions);
				System.out.println("Process ID is " + execution.getProcessInstanceId());
				// mock as if the rest step sets a variable
				execution.setVariable("restResponse", "{}");
				return null;
			}
		}).when(activiti_restCallDelegate).execute((DelegateExecution) any());
	}

	@Test
	public void testProcessExecution() throws Exception {

		ProcessInstance processInstance = activitiRule.getRuntimeService()
				.startProcessInstanceByKey(processDefinitionKey);

		assertNotNull(processInstance);

		//Wait for async step to execute
		unitTestHelpers.waitForJobExecutorToProcessAllJobs(5000, 100);

		verify(activiti_restCallDelegate, times(1)).execute((DelegateExecution) any());

		ProcessInstanceAssert.assertThat(processInstance).isComplete();
	}

}