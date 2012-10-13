package dao;

import java.util.List;

import bean.ConstantDefinition;

public interface IConstantDefinitionDao {
	void insert(ConstantDefinition cd);
	List<ConstantDefinition> query(ConstantDefinition cd);
	void update(ConstantDefinition cd);
	void delete(ConstantDefinition cd);
	
}
