package org.stoevesand.brain.chart;

import java.sql.SQLException;

import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.context.FacesContext;

import org.jboss.logging.Logger;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.jdbc.JDBCXYDataset;
import org.jfree.data.xy.XYDataset;
import org.stoevesand.brain.BrainSession;
import org.stoevesand.brain.BrainSystem;
import org.stoevesand.brain.auth.Authorization;
import org.stoevesand.brain.auth.User;
import org.stoevesand.brain.persistence.BrainDB;

@ManagedBean
@ApplicationScoped
public class Chart {

	private static Logger log = Logger.getLogger(Chart.class);

  @ManagedProperty(value="#{brainSystem}")
  private BrainSystem brainSystem;

  @ManagedProperty(value="#{brainSession}")
  private BrainSession brainSession;

	DefaultCategoryDataset dummy = null;

	// JDBCXYDataset xyd = null;

	public Chart() {
		dummy = new DefaultCategoryDataset();

		dummy.addValue(10, "Classes", "1");
		dummy.addValue(20, "Classes", "12");
		dummy.addValue(30, "Classes", "1234");
		dummy.addValue(40, "Classes", "1423");
		dummy.addValue(30, "Classes1", "1");
		dummy.addValue(40, "Classes1", "12");
		dummy.addValue(10, "Classes1", "1234");
		dummy.addValue(80, "Classes1", "1423");

		BrainDB db = brainSystem.getBrainDB();
		// try {
		// // xyd = new JDBCXYDataset(db.getConnection());
		// } catch (SQLException e) {
		// e.printStackTrace();
		// }

		log.debug("INIT DS");
	}

	public static XYDataset getScoreDataset() {

		BrainSession session = BrainSession.getBrainSession();

		return session.getScoreDataset();

	}

	public JDBCXYDataset getDBScoreDataset() {

		JDBCXYDataset xyd = null;
		try {
			User user = brainSession.getCurrentUser();
			// xyd.executeQuery("select id, score1 as 'Level 1', score2 as 'Level 2',
			// score3 as 'Level 3', score4 as 'Level 4', score5 as 'Level 5',
			// score2+score3*2+score4*4+score5*8 as 'Score' from stats where
			// userID="+user.getId());
			BrainDB db = brainSystem.getBrainDB();
			try {
				xyd = new JDBCXYDataset(db.getConnection("ChartScore"));
			} catch (Exception e) {
				e.printStackTrace();
			}
			// xyd.executeQuery("select logindate, score2+score3*2+score4*3+score5*4
			// as 'Score' from stats where logindate>=subdate(now(), interval 30 day)
			// and userID="+user.getId());
			xyd.executeQuery("select logindate, score as 'Score' from stats where logindate>=subdate(now(), interval 30 day) and userID=" + user.getId());
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return xyd;
	}

	public DefaultCategoryDataset getDummy() {
		log.debug("GET DS");
		return dummy;
	}

}
