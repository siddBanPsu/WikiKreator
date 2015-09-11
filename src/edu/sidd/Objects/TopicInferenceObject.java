package edu.sidd.Objects;

import cc.mallet.topics.TopicInferencer;
import cc.mallet.types.InstanceList;


public class TopicInferenceObject {

	InstanceList instances;
	TopicInferencer topicInferencer;
	public InstanceList getInstances() {
		return instances;
	}
	public void setInstances(InstanceList instances) {
		this.instances = instances;
	}
	public TopicInferencer getTopicInferencer() {
		return topicInferencer;
	}
	public void setTopicInferencer(TopicInferencer topicInferencer) {
		this.topicInferencer = topicInferencer;
	}
	
	
	
}
