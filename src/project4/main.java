package project4;
import mpi.*; 
public class main {
	public static void main(String[] args) 
	{
		MPI.Init(args);
		int num = 2;
		if (args.length > 0) {
			num = Integer.parseInt(args[0]);
		}
		String input_point = "./input/cluster.csv";
		String output_point = "./output_point.csv";
		clusterPoint tmp = new clusterPoint();
		tmp.readCSV(input_point);
		tmp.start();
		MPI.Finalize(); 
	}
}
