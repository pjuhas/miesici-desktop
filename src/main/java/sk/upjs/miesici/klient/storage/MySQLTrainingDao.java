package sk.upjs.miesici.klient.storage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;

import sk.upjs.miesici.admin.storage.Customer;

public class MySQLTrainingDao implements TrainingDao {

	private JdbcTemplate jdbcTemplate;
	private Customer customer; 


	public void setCustomer(Customer customer) {
		this.customer = customer;
	}

	public MySQLTrainingDao(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@Override
	public List<Training> getAll() {
		String sql = "SELECT id, klient_id, nazov, datum, dayofweek(datum) as den, poznamka " +
				   "FROM trening " +
				           "JOIN klient " +
				   "USING (klient_id)";
		List<Training> result = jdbcTemplate.query(sql, new TrainingResultSetExtractor());
		return result;
	}

	@Override
	public void saveTraining(Training training) {
		SimpleJdbcInsert insert = new SimpleJdbcInsert(jdbcTemplate);
		insert.withTableName("trening").usingGeneratedKeyColumns("id");
		Map<String, Object> values = new HashMap<String, Object>();
		values.put("klient_id", training.getClientId());
		values.put("datum", training.getDate());
		values.put("nazov", training.getName());
		values.put("poznamka", training.getNote());
		Number key = insert.executeAndReturnKey(new MapSqlParameterSource(values));
		training.setId(key.longValue());
		//return training;
	}

	@Override
	public List<Training> getAllbyClientId(long clientId) {
		List<Training> all = getAll();
		List<Training> byId = new ArrayList<Training>();
		for(Training training : all) {
			if(training.getClientId() == clientId) {
				byId.add(training);
			}
		}
		return byId;
	}


}
