package project4;


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import mpi.*;

public class clusterPoint {

	public   ArrayList<point> [] clusters;
	public int numofclusters;
	//public ArrayList<point> centers;
	public  point[] centers;
	public double max_X = Double.MIN_VALUE;
	public double min_X = Double.MAX_VALUE;
	public double max_Y = Double.MIN_VALUE;
	public double min_Y = Double.MAX_VALUE;
	public int count;
	public point [] center = new point [1];
	public ArrayList<point>[] group = new ArrayList [1];
	public ArrayList<point> points;
	public void start ()
	{
		//first get max and min
		for (point p : points)
		{
			double x = p.x;
			double y = p.y;
			if (x < min_X)
				min_X = x;
			if (x > max_X)
				max_X = x;
			if (y < min_Y)
				min_Y = y;
			if (y > max_Y)
				max_Y = y;
		}
		
		centers = new point [numofclusters];
		
		
		Random random = new Random();
		for (int i = 0; i < numofclusters; i++) {
			double randomX = min_X + random.nextDouble() * (max_X - min_X);
			double randomY = min_Y + random.nextDouble() * (max_Y - min_Y);
			point p = new point();
			p.x = randomX;
			p.y = randomY;
			centers[i] = p;
		}
		
		
		int rank;
		int size;
		rank = MPI.COMM_WORLD.Rank();
		size = MPI.COMM_WORLD.Size();
		int numofeachslave = points.size() / (size - 1) ;
		
		for (int i = 0; i< count ; i++)
		{
			for (int j = 0; j < numofclusters; j++)
				clusters[j] = new ArrayList<point>();
			
			int[] nearestCenter = new int[numofeachslave];
				
			/*slave*/
			if (rank != 0 )
			{
				MPI.COMM_WORLD.Recv(centers, 0, numofclusters, MPI.OBJECT, 0, 0);
			
				for (int j = 0; j < numofeachslave; j++)
				{
					nearestCenter[j] = getNearestCenter( points.get(  (rank - 1)* numofeachslave  + j )  );
				}
				//send to master
				MPI.COMM_WORLD.Send(nearestCenter, 0, numofeachslave, MPI.INT, 0, 1);
				MPI.COMM_WORLD.Recv(group, 0, 1, MPI.OBJECT, 0, 2);
				center [0] = calculateCenter(group[0]);
				
				MPI.COMM_WORLD.Send(center, 0, 1, MPI.OBJECT, 0, 3);
			}
			//master
			else
			{
				int []  nearestCenterAll = new int [points.size()];
				for (int j = 1; j < size; j++) {
					MPI.COMM_WORLD.Send(centers, 0, numofclusters, MPI.OBJECT, j, 0);
				}
				
				for (int j = 1; j < size; j++){
					MPI.COMM_WORLD.Recv(nearestCenter, 0 , numofeachslave, MPI.INT, j, 1 );	
					for (int k = 0; k < numofeachslave;  k++) {
						nearestCenterAll[((j - 1) * numofeachslave) + k] = nearestCenter[k];
					}
					

				}
				
				for (int j = 0; j < points.size(); j++) {
					clusters[ nearestCenterAll[j]   ].add(points.get(j));
				}
				
				
				
				for (int j = 1; j < size; j++) {
					group[0] = clusters[j - 1];
					MPI.COMM_WORLD.Send(group, 0, 1, MPI.OBJECT, j, 2);
				}
				
				for (int j = 1; j < size; j++) {
					MPI.COMM_WORLD.Recv(center, 0 , 1, MPI.OBJECT, j, 3 );	
					centers[j-1] = center[0];
					
				}


			}
		
		}
		
		
		
		
	}
	public int getNearestCenter(point p)
	{
		int index = -1;
		//int len = this.centroids.length;
		double min = Double.MAX_VALUE;
		//double distance;
		
		for (int i = 0 ; i < centers.length; i++ )
		{
			point c = centers[i];
			double distance = Math.pow(   c.x - p.x , 2) + Math.pow( c.y - p.y, 2);
			if (distance < min)
			{
				min = distance;
				index = i;
			}
		}
		return index;
	}
	public point calculateCenter(ArrayList<point> list)
	{
		if (list.size() == 0)
		{
			//assign random
			Random random = new Random();
			double randomX = min_X + random.nextDouble() * (max_X - min_X);
			double randomY = min_Y + random.nextDouble() * (max_Y - min_Y);
			point  p = new point();
			p.x = randomX;
			p.y = randomY;
			return p;	
		}
		else
		{
			double sumX = 0;
			double sumY = 0;
			for (point p : points) {
				sumX += p.x;
				sumY += p.y;
			}
			point p = new point ();
			p.x = sumX/ list.size();
			p.y = sumY /list.size();
			return p;

		}	
	}
	
	public void readCSV(String input)
	{
		BufferedReader br = new BufferedReader(new FileReader(input));
		String line = "";
		String[] values = null;
		while ((line = br.readLine()) != null) {
			values = line.split(",");
			point p = new point();
			p.x = Double.parseDouble( values[0] );
			p.y = Double.parseDouble( values[1] ) ;
			points.add(p);
		}
		
		
	}
	
}
