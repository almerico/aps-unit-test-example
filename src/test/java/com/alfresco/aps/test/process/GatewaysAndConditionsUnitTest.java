package com.alfresco.aps.test.process;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.activiti.engine.repository.Deployment;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.alfresco.aps.testutils.AbstractTest;
import com.alfresco.aps.testutils.resources.ActivitiResources;

import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import static org.junit.Assert.*;
import static com.alfresco.aps.testutils.TestUtilsConstants.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:activiti.cfg.xml", "classpath:common-beans-and-mocks.xml" })
@TestPropertySource(value="classpath:local-dev-test.properties")
public class GatewaysAndConditionsUnitTest extends AbstractTest {

	String appName = "Test App";
	String processDefinitionKey = "GatewaysAndConditions";
	
	@Before
	public void before() throws Exception {
		
		if(env.getProperty ("aps.app.download", Boolean.class, false)){
			ActivitiResources.forceGet(appName);
		}

		Iterator<File> it = FileUtils.iterateFiles(new File(BPMN_RESOURCE_PATH), null, false);
		while (it.hasNext()) {
			String bpmnXml = ((File) it.next()).getPath();
			String extension = FilenameUtils.getExtension(bpmnXml);
			if (extension.equals("xml")) {
				repositoryService.createDeployment()
						.addInputStream(bpmnXml, new FileInputStream(bpmnXml)).deploy();
			}
		}
		
	}

	@After
	public void after() {
		List<Deployment> deploymentList = activitiRule.getRepositoryService().createDeploymentQuery().list();
		for (Deployment deployment : deploymentList) {
			activitiRule.getRepositoryService().deleteDeployment(deployment.getId(), true);
		}
	}

	@Test
	public void testProcessExecutionApprovePath() throws Exception {
		
		Map<String, Object> processVars = new HashMap<String, Object>();
		processVars.put("initiator", "$INITIATOR");
		ProcessInstance processInstance = activitiRule.getRuntimeService().startProcessInstanceByKey(processDefinitionKey, processVars);

		assertNotNull(processInstance);
		
		assertEquals(1, taskService.createTaskQuery().count());

		Task task = taskService.createTaskQuery().singleResult();
		
		unitTestHelpers.assertUserAssignment("$INITIATOR", task, false, false);
		
		Map<String, Object> taskCompleteVars = new HashMap<String, Object>();
		processVars.put(unitTestHelpers.getTaskOutcomeVariable(task), "Approve");
		
		taskService.complete(task.getId(), taskCompleteVars);

		unitTestHelpers.assertNullProcessInstance(processInstance.getProcessInstanceId());
	}
	
	@Test
	public void testProcessExecutionRejectPath() throws Exception {
		
		Map<String, Object> processVars = new HashMap<String, Object>();
		processVars.put("initiator", "$INITIATOR");
		ProcessInstance processInstance = activitiRule.getRuntimeService().startProcessInstanceByKey(processDefinitionKey, processVars);

		assertNotNull(processInstance);
		
		assertEquals(1, taskService.createTaskQuery().count());

		Task task = taskService.createTaskQuery().singleResult();
		
		unitTestHelpers.assertUserAssignment("$INITIATOR", task, false, false);
		
		
		Map<String, Object> taskCompleteVars = new HashMap<String, Object>();
		taskCompleteVars.put(unitTestHelpers.getTaskOutcomeVariable(task), "Reject");
		taskService.complete(task.getId(), taskCompleteVars);
		
		
		assertEquals(1, taskService.createTaskQuery().count());
		Task rejectTask = taskService.createTaskQuery().singleResult();
		assertEquals("rejected", rejectTask.getName());
		unitTestHelpers.assertUserAssignment("$INITIATOR", rejectTask, false, false);
		taskService.complete(rejectTask.getId());
		
		unitTestHelpers.assertNullProcessInstance(processInstance.getProcessInstanceId());
	}

}