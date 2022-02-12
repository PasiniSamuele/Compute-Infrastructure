package it.polimi.mw.compinf.util;

import akka.actor.ActorSystem;
import akka.dispatch.PriorityGenerator;
import akka.dispatch.UnboundedStablePriorityMailbox;
import com.typesafe.config.Config;
import it.polimi.mw.compinf.tasks.Task;

class PriorityMailbox extends UnboundedStablePriorityMailbox {

    public PriorityMailbox(ActorSystem.Settings settings, Config config) {
        super(new PriorityGenerator() {
            @Override
            public int gen(Object taskObj) {
                Task task = (Task) taskObj;
                return task.getPriority();
            }
        });
    }
}
