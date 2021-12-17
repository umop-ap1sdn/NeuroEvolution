package tsp;

public class Map {

	int cityNum;
	double[][] locations;
	double[][] distanceTable;
	
	public Map(int cityNum, double range) {
		this.cityNum = cityNum;
		locations = new double[cityNum][2];
		distanceTable = new double[cityNum][cityNum];
		
		generate(range);
	}
	
	public void generate(double range) {
		
		
		for(int x = 0; x < cityNum; x++) {
			double randX = Math.random() * range;
			double randY = Math.random() * range;
			
			locations[x] = new double[] {randX, randY};
		}
		
		for(int y = 0; y < cityNum; y++) {
			for(int x = 0; x < cityNum; x++) {
				distanceTable[x][y] = distance(locations[x], locations[y]);
			}
		}
	}
	
	public double distance(double[] point1, double[] point2) {
		double x1 = point1[0];
		double y1 = point1[1];
		double x2 = point2[0];
		double y2 = point2[1];
		
		double dis = Math.pow((x1 - x2), 2) + Math.pow((y1 - y2), 2);
		dis = Math.sqrt(dis);
		
		return dis;
	}
	
	public double[][] getDistance() {
		return this.distanceTable;
	}
	
	public double[][] getLocations(){
		return this.locations;
	}
	
	public double[] location1D() {
		double[] loc1D = new double[locations.length * locations[0].length];
		
		int count = 0;
		for(double[] coords: locations) {
			for(double n: coords) {
				loc1D[count] = n;
				count++;
			}
		}
		
		
		return loc1D;
	}
}
