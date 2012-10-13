package service.impl;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import dao.IConstantDefinitionDao;

import bean.ConstantDefinition;
import service.IConstantDefinitionService;

@Component
public class ConstantDefinitionServiceImpl implements IConstantDefinitionService{

	private IConstantDefinitionDao constantDefinitionDao;
	
	@Override
	public void insertConstantDefinition(ConstantDefinition cd) {
		this.constantDefinitionDao.insert(cd);
	}

	@Override
	public void updateConstantDefinition(ConstantDefinition cd) {
		this.constantDefinitionDao.update(cd);
	}

	@Override
	public void removeConstantDefinition(ConstantDefinition cd) {
		this.constantDefinitionDao.delete(cd);
	}

	@Override
	public List<ConstantDefinition> queryConstantDefinition(
			ConstantDefinition cd) {
		
		return this.constantDefinitionDao.query(cd);
	}

	@Resource(name="constantDefinitionDaoImpl")
	public void setConstantDefinitionDao(
			IConstantDefinitionDao constantDefinitionDao) {
		this.constantDefinitionDao = constantDefinitionDao;
	}

}
