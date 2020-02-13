package org.webcurator.core.harvester.agent;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;
import org.webcurator.domain.model.core.harvester.agent.HarvestAgentStatusDTO;

import java.util.List;
import java.util.Map;

@RestController
public class HarvestAgentController implements HarvestAgent {

    @Autowired
    @Qualifier("harvestAgent")
    private HarvestAgent harvestAgent;

    @Override
    @GetMapping(path = HarvestAgentPaths.NAME)
    public String getName() {
        return harvestAgent.getName();
    }

    @Override
    @PostMapping(path = HarvestAgentPaths.INITIATE_HARVEST)
    public void initiateHarvest(@PathVariable(value = "job") String job,
                                @RequestBody Map<String, String> params) {
        harvestAgent.initiateHarvest(job, params);
    }

    @Override
    @PostMapping(path = HarvestAgentPaths.RECOVER_HARVESTS)
    public void recoverHarvests(@RequestParam(value = "active-jobs") List<String> activeJobs) {
        harvestAgent.recoverHarvests(activeJobs);
    }

    @Override
    @PostMapping(path = HarvestAgentPaths.UPDATE_PROFILE_OVERRIDES)
    public void updateProfileOverrides(@PathVariable(value = "job") String job,
                                       @RequestParam(value = "profile") String profile) {
        harvestAgent.updateProfileOverrides(job, profile);
    }

    @Override
    @PostMapping(path = HarvestAgentPaths.PURGE_ABORTED_TARGET_INSTANCES)
    public void purgeAbortedTargetInstances(@RequestParam(value = "target-instance-names") List<String> targetInstanceNames) {
        harvestAgent.purgeAbortedTargetInstances(targetInstanceNames);
    }

    @Override
    @PostMapping(path = HarvestAgentPaths.RESTRICT_BANDWIDTH)
    public void restrictBandwidth(@PathVariable(value = "job") String job,
                                  @RequestParam(value = "bandwidth-limit") int bandwidthLimit) {
        harvestAgent.restrictBandwidth(job, bandwidthLimit);
    }

    @Override
    @PostMapping(path = HarvestAgentPaths.PAUSE)
    public void pause(@PathVariable(value = "job") String job) {
        harvestAgent.pause(job);
    }

    @Override
    @PostMapping(path = HarvestAgentPaths.RESUME)
    public void resume(@PathVariable(value = "job") String job) {
        harvestAgent.resume(job);
    }

    @Override
    @PostMapping(path = HarvestAgentPaths.ABORT)
    public void abort(@PathVariable(value = "job") String job) {
        harvestAgent.abort(job);
    }

    @Override
    @PostMapping(path = HarvestAgentPaths.STOP)
    public void stop(@PathVariable(value = "job") String job) {
        harvestAgent.stop(job);
    }

    @Override
    @PostMapping(path = HarvestAgentPaths.COMPLETE_HARVEST)
    public int completeHarvest(@PathVariable(value = "job") String job,
                               @RequestParam(value = "failure-step") int failureStep) {
        // TODO Note that the SOAP version did a harvestAgent.stop(job) and then returned HarvestAgent.NO_FAILURES
        return harvestAgent.completeHarvest(job, failureStep);
    }

    @Override
    @PostMapping(path = HarvestAgentPaths.LOAD_SETTINGS)
    public void loadSettings(@PathVariable(value = "job") String job) {
        harvestAgent.loadSettings(job);
    }

    @Override
    @PostMapping(path = HarvestAgentPaths.PAUSE_ALL)
    public void pauseAll() {
        harvestAgent.pauseAll();
    }

    @Override
    @PostMapping(path = HarvestAgentPaths.RESUME_ALL)
    public void resumeAll() {
        harvestAgent.resumeAll();
    }

    @Override
    @GetMapping(path = HarvestAgentPaths.MEMORY_WARNING)
    public boolean getMemoryWarning() {
        return harvestAgent.getMemoryWarning();
    }

    @Override
    @PostMapping(path = HarvestAgentPaths.MEMORY_WARNING)
    public void setMemoryWarning(@RequestParam(value = "memory-warning") boolean memoryWarning) {
        harvestAgent.setMemoryWarning(memoryWarning);
    }

    @Override
    @GetMapping(path = HarvestAgentPaths.STATUS)
    public HarvestAgentStatusDTO getStatus() {
        return harvestAgent.getStatus();
    }

    @Override
    @GetMapping(path = HarvestAgentPaths.IS_VALID_PROFILE)
    public boolean isValidProfile(@PathVariable(value = "profile") String profile) {
        return harvestAgent.isValidProfile(profile);
    }

    @Override
    @PostMapping(path = HarvestAgentPaths.EXECUTE_SHELL_SCRIPT)
    public HarvestAgentScriptResult executeShellScript(@PathVariable(value = "job-name") String jobName,
                                                       @RequestParam(value = "engine") String engine,
                                                       @RequestParam(value = "shell-script") String shellScript) {
        throw new RuntimeException("The execution of shell scripts is not supported by Heritrix 1");
    }
}
