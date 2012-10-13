package service;

import java.util.List;

import bean.ConstantDefinition;

public interface IConstantDefinitionService {
	void insertConstantDefinition(ConstantDefinition cd);
	void updateConstantDefinition(ConstantDefinition cd);
	void removeConstantDefinition(ConstantDefinition cd);
	List<ConstantDefinition> queryConstantDefinition(ConstantDefinition cd);
}
