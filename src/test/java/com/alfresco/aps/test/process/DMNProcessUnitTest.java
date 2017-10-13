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

	/*
	 * Setting the App name to be downloaded if run with -Daps.app.download=true
	 * Also set the process definition key of the process that is being tested
	 */
	static {
		appName = "Test App";
		processDefinitionKey = "DMNProcess";
	}

	/*
	 * Test testing a success path of Start->Rules->Gateway->End
	 */
	@Test
	public void testRuleExecutionSuccessPath() throws Exception {

		/*
		 * Reset mocks that we will be asserting later in the test
		 */
		Mockito.reset(activiti_executeDecisionDelegate);

		/*
		 * A listener for mock to do those things that the DMN step would
		 * normally do in this test. eg: In this test to take a success path, we
		 * want the DMN step to set a variable "output" with value "abc"
		 */
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

		/*
		 * Starting the process using processDefinitionKey
		 */
		ProcessInstance processInstance = activitiRule.getRuntimeService()
				.startProcessInstanceByKey(processDefinitionKey);

		/*
		 * Once started assert that the process instance is not null and
		 * successfully started
		 */
		assertNotNull(processInstance);

		/*
		 * Since the next step is a DMN Rule execution, assert that the
		 * corresponding APS Java Delegate responsible for running the rule is
		 * invoked only once
		 */
		verify(activiti_executeDecisionDelegate, times(1)).execute((DelegateExecution) any());

		/*
		 * Using the custom assertion ProcessInstanceAssert, make sure that the
		 * process is now ended. Expected as per the success path.
		 */
		ProcessInstanceAssert.assertThat(processInstance).isComplete();
	}

	/*
	 * Test testing a fail path of Start->Rules->Gateway->User Task->End
	 */
	@Test
	public void testRuleExecutionFailPath() throws Exception {

		/*
		 * Reset mocks that we will be asserting later in the test
		 */
		Mockito.reset(activiti_executeDecisionDelegate);

		/*
		 * A listener for mock to do those things that the DMN step would
		 * normally do in this test. eg: In this test to take a reject path, we
		 * want the DMN step to not set any variables to mock "none of the rules are
		 * evaluated"
		 */
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

		/*
		 * Starting the process using processDefinitionKey
		 */
		ProcessInstance processInstance = activitiRule.getRuntimeService()
				.startProcessInstanceByKey(processDefinitionKey);

		/*
		 * Once started assert that the process instance is not null and
		 * successfully started
		 */
		assertNotNull(processInstance);

		/*
		 * Since the next step is a DMN Rule execution, assert that the
		 * corresponding APS Java Delegate responsible for running the rule is
		 * invoked only once
		 */
		verify(activiti_executeDecisionDelegate, times(1)).execute((DelegateExecution) any());

		/*
		 * Since the next step in the reject path is a user task, doing a query
		 * to find the user task count in the engine. Assert that it is only 1
		 */
		assertEquals(1, taskService.createTaskQuery().count());

		/*
		 * Get the Task object for further task assertions
		 */
		Task rejectTask = taskService.createTaskQuery().singleResult();

		/*
		 * Asserting the task for things such as assignee, task name etc. Also,
		 * at the end of it complete the task Using the custom assertion
		 * TaskAssert from the utils project here
		 */
		TaskAssert.assertThat(rejectTask).hasAssignee("$INITIATOR", false, false).hasName("Rule Not Evaluated")
				.complete();

		/*
		 * Using the custom assertion ProcessInstanceAssert, make sure that the
		 * process is now ended after the user task is completed.
		 */
		ProcessInstanceAssert.assertThat(processInstance).isComplete();
	}

}