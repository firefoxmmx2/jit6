package dao.hibernate;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.criterion.DetachedCriteria;
import org.springframework.stereotype.Component;

import bean.ConstantDefinition;
import dao.IConstantDefinitionDao;
@Component
public class ConstantDefinitionDaoImpl extends MysqlTestDaoImpl implements IConstantDefinitionDao {

	@Override
	public void insert(ConstantDefinition cd) {
		this.getHibernateTemplate().save(cd);
	}

	@Override
	public List<ConstantDefinition> query(ConstantDefinition cd) {
		DetachedCriteria criteria = DetachedCriteria.forClass(ConstantDefinition.class);
		return this.getHibernateTemplate().findByExample(cd);
	}

	@Override
	public void update(ConstantDefinition cd) {
		this.getHibernateTemplate().update(cd);
	}

	@Override
	public void delete(ConstantDefinition cd) {
		this.delete(cd);
	}
	
}
