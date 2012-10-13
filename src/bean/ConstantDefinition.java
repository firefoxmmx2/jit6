package bean;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.Table;


@Entity
@org.hibernate.annotations.Entity(dynamicUpdate=true)
@Table(name="constant_definition")
public class ConstantDefinition {
	@Id
	@GeneratedValue
	private Integer id;
	@Column(length=200)
	private String constantPrefix;
	@Column(length=200)
	private String constantValue;
	@Column(length=500)
	private String comments;
	@Column(length=200)
	private String constantDictCode;
	@Column(length=200)
	private String constantType;
	@ManyToOne
	@JoinColumn(name="constant_template_id",referencedColumnName="id")
	private ConstantTemplate template;
	
	
	public Integer getId() {
		return id;
	}
	
	public void setId(Integer id) {
		this.id = id;
	}
	public String getConstantPrefix() {
		return constantPrefix;
	}
	public void setConstantPrefix(String constantPrefix) {
		this.constantPrefix = constantPrefix;
	}
	public String getConstantValue() {
		return constantValue;
	}
	public void setConstantValue(String constantValue) {
		this.constantValue = constantValue;
	}
	public String getConstantDictCode() {
		return constantDictCode;
	}
	public void setConstantDictCode(String constantDictCode) {
		this.constantDictCode = constantDictCode;
	}
	public String getConstantType() {
		return constantType;
	}
	public void setConstantType(String constantType) {
		this.constantType = constantType;
	}
	public String getComments() {
		return comments;
	}
	public void setComments(String comments) {
		this.comments = comments;
	}

	public ConstantTemplate getTemplate() {
		return template;
	}

	public void setTemplate(ConstantTemplate template) {
		this.template = template;
	}
	
	
}
