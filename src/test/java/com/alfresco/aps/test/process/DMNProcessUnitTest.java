package com.alfresco.aps.test.process;

import java.util.HashMap;
import org.activiti.engine.delegate.DelegateExecution;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.alfresco.aps.testutils.AbstractBpmnTest;
import com.alfresco.aps.testutils.assertions.DelegateExecutionAssert;
import com.alfresco.aps.testutils.assertions.ProcessInstanceAssert;
import com.alfresco.aps.testutils.assertions.TaskAssert;

import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:activiti.cfg.xml", "classpath:common-beans-and-mocks.xml" })
public class DMNProcessUnitTest extends AbstractBpmnTest {

	static {
		appName = "Test App";
		processDefinitionKey = "DMNProcess";
	}

	@Test
	public void testRuleExecutionSuccessPath() throws Exception {
		Mockito.reset(activiti_executeDecisionDelegate);

		doAnswer(new Answer<Void>() {
			@Override
			public Void answer(InvocationOnMock invocation) throws Throwable {

				Object[] arg = invocation.getArguments();
				DelegateExecution execution = (DelegateExecution) arg[0];
				HashMap<String, String> fieldExtensions = new HashMap<String, String>();
				fieldExtensions.put("decisionTableReferenceKey", "dmntest");
				DelegateExecutionAssert.assertThat(execution).assertFieldExtensions(1, fieldExtensions);
				// mock as if the dmn step sets a variable
				execution.setVariable("output", "abc");
				return null;
			}
		}).when(activiti_executeDecisionDelegate).execute((DelegateExecution) any());

		ProcessInstance processInstance = activitiRule.getRuntimeService()
				.startProcessInstanceByKey(processDefinitionKey);

		assertNotNull(processInstance);

		verify(activiti_executeDecisionDelegate, times(1)).execute((DelegateExecution) any());

		ProcessInstanceAssert.assertThat(processInstance).isComplete();
	}

	@Test
	public void testRuleExecutionFailPath() throws Exception {

		Mockito.reset(activiti_executeDecisionDelegate);

		doAnswer(new Answer<Void>() {
			@Override
			public Void answer(InvocationOnMock invocation) throws Throwable {

				Object[] arg = invocation.getArguments();
				DelegateExecution execution = (DelegateExecution) arg[0];
				HashMap<String, String> fieldExtensions = new HashMap<String, String>();
				fieldExtensions.put("decisionTableReferenceKey", "dmntest");
				DelegateExecutionAssert.assertThat(execution).assertFieldExtensions(1, fieldExtensions);
				// do not set any variable as if dmn rules didn't pass
				return null;
			}
		}).when(activiti_executeDecisionDelegate).execute((DelegateExecution) any());

		ProcessInstance processInstance = activitiRule.getRuntimeService()
				.startProcessInstanceByKey(processDefinitionKey);

		assertNotNull(processInstance);

		verify(activiti_executeDecisionDelegate, times(1)).execute((DelegateExecution) any());

		assertEquals(1, taskService.createTaskQuery().count());

		Task rejectTask = taskService.createTaskQuery().singleResult();

		TaskAssert.assertThat(rejectTask).hasAssignee("$INITIATOR", false, false)
				.hasName("Rule Not Evaluated").complete();

		ProcessInstanceAssert.assertThat(processInstance).isComplete();
	}

}