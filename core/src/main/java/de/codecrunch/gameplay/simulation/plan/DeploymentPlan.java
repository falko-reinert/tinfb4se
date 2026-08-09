package de.codecrunch.gameplay.simulation.plan;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** Immutable list of requested deployments. */
public final class DeploymentPlan {
    private final List<Deployment> deployments;

    public DeploymentPlan(List<Deployment> deployments) {
        if (deployments == null) {
            throw new IllegalArgumentException("deployments are required");
        }
        this.deployments = Collections.unmodifiableList(new ArrayList<>(deployments));
    }

    public static DeploymentPlan of(Deployment... deployments) {
        return new DeploymentPlan(Arrays.asList(deployments));
    }

    public List<Deployment> getDeployments() {
        return deployments;
    }
}
