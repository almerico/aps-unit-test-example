package com.alfresco.aps.test.process;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.alfresco.aps.testutils.AbstractBpmnTest;
import com.alfresco.aps.testutils.assertions.ProcessInstanceAssert;

import org.activiti.engine.runtime.ProcessInstance;
import static org.junit.Assert.*;
import static com.alfresco.aps.testutils.TestUtilsConstants.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:activiti.cfg.xml", "classpath:common-beans-and-mocks.xml" })
public class BoundaryTimerCatchProcessUnitTest extends AbstractBpmnTest {

	static {
		appName = "Test App";
		processDefinitionKey = "BoundaryTimerCatchProcess";
	}

	@Test
	public void testProcessExecution() throws Exception {

		ProcessInstance processInstance = activitiRule.getRuntimeService()
				.startProcessInstanceByKey(processDefinitionKey);

		assertNotNull(processInstance);

		ProcessInstanceAssert.assertThat(processInstance).receiveTaskCountIs(2).executeReceiveTasks().isComplete();
	}

	@Test
	public void testProcessExecutionViaBoundary() throws Exception {

		ProcessInstance processInstance = activitiRule.getRuntimeService()
				.startProcessInstanceByKey(processDefinitionKey);

		assertNotNull(processInstance);

		ProcessInstanceAssert.assertThat(processInstance).timerJobCountIs(2)
				.timerJobsWithDueDateFromNow(5, TIME_UNIT_MINUTE).timerJobsWithDueDateFromNow(1, TIME_UNIT_DAY)
				.executeTimerJobs(5, TIME_UNIT_MINUTE).executeTimerJobs(1, TIME_UNIT_DAY).isComplete();
	}

}