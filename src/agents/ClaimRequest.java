package agents;

import java.awt.Point;
import java.io.Serializable;

public class ClaimRequest implements Serializable {
	public Point point;
	public double distance;

	public ClaimRequest(Point point, double d) {
		this.point = point;
		this.distance = d;
	}
}
