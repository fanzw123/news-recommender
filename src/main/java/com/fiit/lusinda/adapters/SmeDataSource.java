package com.fiit.lusinda.adapters;

import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fiit.lusinda.translate.TranslateStrategy;
import com.fiit.lusinda.utils.Logging;

public class SmeDataSource {

	public static Connection conn;

	public SmeDataSource() {

	}

	public void connect() {
		connect("root", "root", "jdbc:mysql://localhost/sme.sk");

	}

	public void connect(String userName, String userPassword, String databaseUrl) {
		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			conn = DriverManager.getConnection(databaseUrl, userName,
					userPassword);

		} catch (SQLException e) {
			e.printStackTrace();

		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void disconnect() {
		try {
			conn.close();
			Logging.Log("disconnected");

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			Logging.Log(e.getMessage());
		}
	}

	public int getCount(String stm) throws SQLException, IOException {

		PreparedStatement statement = conn.prepareStatement(stm);// +String.valueOf(numberOfArticles));

		ResultSet result = statement.executeQuery();
		int count = -1;

		while (result.next()) {
			count = result.getInt("count");

		}

		return count;

	}

	public void resetTimeSlices() throws SQLException
	{
		PreparedStatement resetStm = conn
				.prepareStatement("UPDATE Articles SET time_slice_id=NULL");

		resetStm.executeUpdate();

	}
	
	public int preprocessTimeSlice(int minSliceSize, int minDateInterval) throws SQLException {

		// get first not processed article
		PreparedStatement getLastSliceStm = conn
				.prepareStatement("SELECT published_at,max(published_at) as last_published  FROM articles where time_slice_id IS NULL order by published_at LIMIT 1");

		ResultSet result = getLastSliceStm.executeQuery();

		result.first();
		Date sliceStart = result.getDate("published_at");
		Date lastPublishedDate = result.getDate("last_published");

		getLastSliceStm.close();

		PreparedStatement getMaxLastSliceIdStm = conn
				.prepareStatement("SELECT max(time_slice_id) as max_slice_id FROM articles");

		result = getMaxLastSliceIdStm.executeQuery();

		result.first();
		
		int timeSliceID = result.getInt("max_slice_id");
		timeSliceID++; //get next slice id
		
		getMaxLastSliceIdStm.close();
		
		PreparedStatement getSliceSizeStm = conn
				.prepareStatement("select if(ADDDATE(?, ?) >= ?,-1, count(published_at)) from articles where time_slice_id IS NULL and published_at between ? and  ADDDATE(?, ?) order by published_at");
		int sliceSize = 0;
		int dateInterval = 0;
		do  {
			dateInterval += minDateInterval; // increment dateIterval size
			
			getSliceSizeStm.setDate(1, sliceStart);
			getSliceSizeStm.setInt(2, dateInterval);
			getSliceSizeStm.setDate(3, lastPublishedDate);
			
			getSliceSizeStm.setDate(4, sliceStart);
			getSliceSizeStm.setDate(5, sliceStart);
			getSliceSizeStm.setInt(6, dateInterval);

			result = getSliceSizeStm.executeQuery();
			result.first();

			sliceSize = result.getInt(1);

		}while(sliceSize < minSliceSize && sliceSize>0);
		
		getSliceSizeStm.close();

		PreparedStatement updateStm = conn
				.prepareStatement("UPDATE Articles SET time_slice_id = ? where time_slice_id IS NULL and  published_at between ? and  ADDDATE(?, ?) order by published_at");

		updateStm.setInt(1, timeSliceID);
		updateStm.setDate(2, sliceStart);
		updateStm.setDate(3, sliceStart);
		updateStm.setInt(4, dateInterval);
		
		int effected =  updateStm.executeUpdate();
		
		updateStm.close();
		
		return effected;

	}

	public void translateArticles(String[] columns, String where,
			TranslateStrategy translator) throws SQLException {

		this.translateArticles(columns, where, -1, translator);
	}

	public void translateArticles(String[] columns, String where, int limit,
			TranslateStrategy translator) throws SQLException {

		StringBuilder insertStm = new StringBuilder(
				"INSERT INTO Articles_translated (Id_Article, ");

		StringBuilder selectStm = new StringBuilder();

		selectStm.append("SELECT ");
		for (String col : columns) {
			selectStm.append(col);
			selectStm.append(",");

			// insert
			insertStm.append(col);
			insertStm.append(",");

		}

		insertStm.deleteCharAt(insertStm.length() - 1);
		insertStm.append(") ");

		insertStm.append("VALUES(?, ");

		for (String col : columns) {

			// insert
			insertStm.append("?");
			insertStm.append(",");

		}
		insertStm.deleteCharAt(insertStm.length() - 1);
		insertStm.append(") ");

		selectStm.append("id");
		selectStm.append(" FROM ");
		selectStm.append("Articles ");
		if(where!=null &&!where.isEmpty())
		{
			selectStm.append("WHERE ");
			selectStm.append(where);
		}
		if (limit > 0) {
			selectStm.append(" LIMIT ");
			selectStm.append(Integer.toString(limit));
		}

		Logging.Log("Select stm:" + selectStm.toString());
		Logging.Log("Insert stm:" + insertStm.toString());

		PreparedStatement selectStatement = conn.prepareStatement(selectStm
				.toString());

		PreparedStatement insertStatement = conn.prepareStatement(insertStm
				.toString());

		PreparedStatement checkStatement = conn
				.prepareStatement("select count(*) FROM Articles_Translated where id_Article=?");

		ResultSet result = selectStatement.executeQuery();

		while (result.next()) {

			int id = result.getInt("id");

			checkStatement.setInt(1, id);
			ResultSet translatedResult = checkStatement.executeQuery();

			translatedResult.next();

			if (translatedResult.getInt(1) == 0) {

				int param = 2;

				insertStatement.setInt(1, id);

				for (String col : columns) {

					// Logging.Log(result.getString(col));

					try {
						String translated = translator.translateText(result
								.getString(col));

						insertStatement.setString(param, translated);

						param++;

					} catch (Exception ex) {
						Logging.Log(String.format(
								"article id: %d. Error msg: %s", id,
								ex.getMessage()));
					}

				}

				insertStatement.executeUpdate();
				Logging.Log(String.format("inserted article with id:%d", id));
			} else
				Logging.Log(String.format("skipping article with id:%d", id));

		}

	}
}
