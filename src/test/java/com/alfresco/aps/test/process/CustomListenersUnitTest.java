package com.alfresco.aps.test.process;

import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.event.ActivitiEvent;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.alfresco.aps.example.listeners.ProcessEndExecutionListener;
import com.alfresco.aps.example.listeners.TaskAssignedTaskListener;
import com.alfresco.aps.example.listeners.TimerFiredEventListener;
import com.alfresco.aps.testutils.AbstractBpmnTest;
import com.alfresco.aps.testutils.ProcessInstanceAssert;

import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import static com.alfresco.aps.testutils.TestUtilsConstants.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:activiti.cfg.xml", "classpath:common-beans-and-mocks.xml", "classpath:process-beans-and-mocks.xml" })
@TestPropertySource(value="classpath:local-dev-test.properties")
public class CustomListenersUnitTest extends AbstractBpmnTest {
	
	static {
		appName = "Test App";
		processDefinitionKey = "CustomListeners";
	}
	
	@Autowired
	TaskAssignedTaskListener taskAssignedTaskListener;
	
	@Autowired
	TimerFiredEventListener timerFiredEventListener;
	
	@Autowired
	ProcessEndExecutionListener processEndExecutionListener;
	
	@Before
	public void beforeTest() throws Exception {

		doAnswer(new Answer<Void>() {
			@Override
			public Void answer(InvocationOnMock invocation) throws Throwable {
				
				Object[] arg = invocation.getArguments();
				DelegateExecution execution = (DelegateExecution) arg[0];
				// mock the listener stuff you require for next process step here (eg:set a variable)
				execution.setVariable("processEndExecutionListener", "executed");
				return null;
			}
		}).when(processEndExecutionListener).notify((DelegateExecution) any());
		
		doAnswer(new Answer<Void>() {
			@Override
			public Void answer(InvocationOnMock invocation) throws Throwable {
				
				Object[] arg = invocation.getArguments();
				ActivitiEvent event = (ActivitiEvent) arg[0];
				assertTrue(event.getType().equals(ActivitiEventType.TIMER_FIRED));
				return null;
			}
		}).when(timerFiredEventListener).onEvent((ActivitiEvent) any());
		
		doAnswer(new Answer<Void>() {
			@Override
			public Void answer(InvocationOnMock invocation) throws Throwable {
				
				Object[] arg = invocation.getArguments();
				DelegateTask task = (DelegateTask) arg[0];
				// mock the listener stuff you require for next process step here (eg:set a variable)
				task.setVariable("taskAssignedTaskListener", "executed");
				return null;
			}
		}).when(taskAssignedTaskListener).notify((DelegateTask) any());
	}

	@Test
	public void testProcessExecution() throws Exception {

		Map<String, Object> processVars = new HashMap<String, Object>();
		processVars.put("initiator", "$INITIATOR");
		ProcessInstance processInstance = activitiRule.getRuntimeService().startProcessInstanceByKey(processDefinitionKey, processVars);

		assertNotNull(processInstance);
		
		assertEquals(1, taskService.createTaskQuery().count());

		Task task = taskService.createTaskQuery().singleResult();
		
		verify(taskAssignedTaskListener, times(1)).notify((DelegateTask) any());
		
		taskService.complete(task.getId());
		
		unitTestHelpers.assertTimerJob(1, 2, TIME_UNIT_MINUTE, true);
		
		verify(timerFiredEventListener, times(1)).onEvent((ActivitiEvent) any());
		
		verify(processEndExecutionListener, times(1)).notify((DelegateExecution) any());
		
		ProcessInstanceAssert.assertThat(processInstance).isComplete();
		
		Map<String, Object> variablesToAssert = new HashMap<String, Object>();
		variablesToAssert.put("taskAssignedTaskListener", "executed");
		variablesToAssert.put("processEndExecutionListener", "executed");
		unitTestHelpers.assertHistoricVariableValues(processInstance.getProcessInstanceId(), variablesToAssert);
	}

}