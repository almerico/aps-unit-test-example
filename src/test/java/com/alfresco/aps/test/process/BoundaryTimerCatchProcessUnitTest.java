package com.alfresco.aps.test.process;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.alfresco.aps.testutils.AbstractBpmnTest;
import org.activiti.engine.runtime.ProcessInstance;
import static org.junit.Assert.*;
import static com.alfresco.aps.testutils.TestUtilsConstants.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:activiti.cfg.xml", "classpath:common-beans-and-mocks.xml" })
@TestPropertySource(value="classpath:local-dev-test.properties")
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
		
		unitTestHelpers.assertReceiveTask(2, true, null, processDefinitionId);

		unitTestHelpers.assertNullProcessInstance(processInstance.getProcessInstanceId());
	}
	
	@Test
	public void testProcessExecutionViaBoundary() throws Exception {

		ProcessInstance processInstance = activitiRule.getRuntimeService()
				.startProcessInstanceByKey(processDefinitionKey);

		assertNotNull(processInstance);


		//Assert in seconds and execute/action timer
		unitTestHelpers.assertTimerJob(1, 5, TIME_UNIT_MINUTE, true);
		//Assert days and execute/action timer
		unitTestHelpers.assertTimerJob(1, 1, TIME_UNIT_DAY, true);

		unitTestHelpers.assertNullProcessInstance(processInstance.getProcessInstanceId());
	}

}