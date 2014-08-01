package project4;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import mpi.*;
public class clusterDNA {
	
	public String [] centers;
	public int num;
	public String [] center = new String [1];
	public ArrayList<String> [] clusters;
	public ArrayList<String> [] group = new ArrayList  [1];
	public ArrayList<String> DNA; 
	public int count ; 
	public int DNAlength; 
	public clusterDNA()
	{
		DNA = new ArrayList<String> ();
	}
	public void start ()
	{
		DNAlength = DNA.get(0).length();
		centers = new String [num];
		for (int i = 0; i< num; i++ )
		{
			centers[i] = generateDNA(DNAlength);
		}
		
		int rank = MPI.COMM_WORLD.Rank(); 
		int size = MPI.COMM_WORLD.Size(); 
		
		for (int i = 0; i < count ; i++)
		{
			for (int j = 0; j < num ; j++)
				clusters[j] = new ArrayList<String> ();
			
			int numofslave = DNA.size() / (size - 1);
			int[] nearestCenter = new int[numofslave];
			int [] nearestCenterAll = new int [DNA.size()];
			if (rank != 0)
			{
				MPI.COMM_WORLD.Recv(centers, 0 , num, MPI.OBJECT, 0, 0);
				for (int j = 0; j < numofslave; j++) {
					nearestCenter[j] = getNearestCenter(DNA.get(  (rank - 1)* numofslave + j ));
				}
				MPI.COMM_WORLD.Send(nearestCenter, 0 , num, MPI.INT, 0, 1);
				MPI.COMM_WORLD.Recv (group, 0, 1, MPI.OBJECT, 0, 2);
				center[0] = getcenter(group[0]);
				MPI.COMM_WORLD.Send (center, 0, 1, MPI.OBJECT, 0, 3);
			}
			else 
			{
				for (int j  = 1; j< size; j++) {
					MPI.COMM_WORLD.Send(centers, 0, num, MPI.OBJECT, j, 0);
				}
				for (int j  = 1; j< size; j++) {

					Status status = MPI.COMM_WORLD.Recv(nearestCenter, 0, numofslave, MPI.INT,MPI.ANY_SOURCE, 1);

					for (int k = 0; k < numofslave; k++) {
						nearestCenterAll[((status.source - 1) * numofslave) + k] = nearestCenter[k];
					}
					
					
					for (int k = 0; k < numofslave; k++) {
						clusters[ nearestCenterAll[k]  ].add(DNA.get(k));
					}

					// parallel recalculate centroids
					for (int j  = 1; j< size; j++)  {
						group[0] = clusters[j - 1];
						MPI.COMM_WORLD.Send(group, 0, 1, MPI.OBJECT, j, 2);
					}

					for (int j  = 1; j< size; j++)  {
						Status status = MPI.COMM_WORLD.Recv(group, 0, 1, MPI.OBJECT, MPI.ANY_SOURCE, 3);
						centers[status.source - 1] = center[0];
					}
					
					

				}

				
				
				
			}
		}
		
		
	}
	public String getcenter(ArrayList<String> list)
	{
		if (list.size() == 0)
			return generateDNA(DNAlength);
		
		else
		{
			char [] tmp = new char [4];
			tmp[0] = 'A';
			tmp[1] = 'C';
			tmp[2] = 'G';
			tmp[3] = 'T';
			String s = "";
			int [] max = new int [4];
			for (int i = 0; i < DNAlength; i++)
			{
				
				for ( String ss : list  )
				{
					if (ss.charAt(i) == 'A')
						max[0] ++ ;
					else if (ss.charAt(i) == 'C')
						max[1] ++ ;
					else if (ss.charAt(i) == 'D')
						max[2] ++ ;
					else 
						max[3] ++;
				}
			}
			int max_value = Integer.MIN_VALUE;
			int pos = -1;
			for (int i = 0; i< 4; i++ )
			{
				if (max_value < max[i]){
					max_value = max[i];
					pos = i;
				}
			}
			s = s + tmp[pos];
			
			return s;
		}
		
	}
	
	
	public int getNearestCenter( String s)
	{
		int index = -1; 
		int min = Integer.MAX_VALUE;
		for (int i = 0; i < centers.length; i++)
		{
			int distance = 0; 
			for (int j = 0; j< DNAlength ; j++)
			{
				if (centers[i].charAt(j) != s.charAt(j))
					distance ++ ;
			}
			if (distance <  min)
			{
				distance = min;
				index = i;
			}
		}
		return index;
	}
	public String generateDNA(int length)
	{
		Random rn = new Random();
		String s = "";
		char [] tmp = new char [4];
		tmp[0] = 'A';
		tmp[1] = 'C';
		tmp[2] = 'G';
		tmp[3] = 'T';
		for (int i = 0; i< length; i++)
		{
			int d = rn.nextInt() % 4;
			s = s + tmp[d];
		}
		return s;
	}
	
	public void readCSV(String input) throws IOException
	{
		BufferedReader br = new BufferedReader(new FileReader(input));
		String line = "";
		String[] values = null;
		
		while ((line = br.readLine()) != null) {
			values = line.split(",");
			DNA.add(values[0]);
		}
		br.close();
	}
	
}
