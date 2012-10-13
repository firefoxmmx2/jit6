package dao.hibernate;

import javax.annotation.Resource;

import org.hibernate.SessionFactory;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import org.springframework.stereotype.Component;

@Component
public class MysqlTestDaoImpl  extends HibernateDaoSupport{
	@Resource(name="sessionFactory")
	public void setTestSessionFactory(SessionFactory sessionFactory){
		super.setSessionFactory(sessionFactory);
	}
}
