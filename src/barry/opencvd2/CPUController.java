package barry.opencvd2;

import java.io.*;


public class CPUController {

	int m_CurrFreq;
	InputStream m_instream;
	InputStreamReader m_inputreader;
	BufferedReader m_buffreader;	
	OutputStream m_outstream;
	OutputStreamWriter m_outputwriter;
	BufferedWriter m_buffwriter;
	Process m_suprocess;
	DataOutputStream os;
	
    final int CPU_FREQ0=100000;
    final int CPU_FREQ1=200000;
    final int CPU_FREQ2=400000;
    final int CPU_FREQ3=1600000;
    final int CPU_FREQ4=1600000;
    final int num_freq = 4;
    int freq_old=0;
    
	/*Constructor*/
	public CPUController(){
		System.out.println("TEST33!!!");
		try{	
			m_suprocess=Runtime.getRuntime().exec("su");
		    os =new DataOutputStream(m_suprocess.getOutputStream());
		    os.writeBytes("echo userspace>/sys/devices/system/cpu/cpu0/cpufreq/scaling_governor\n");
            os.writeBytes("echo userspace>/sys/devices/system/cpu/cpu1/cpufreq/scaling_governor\n");
            os.writeBytes("echo userspace>/sys/devices/system/cpu/cpu2/cpufreq/scaling_governor\n");
            os.writeBytes("echo userspace>/sys/devices/system/cpu/cpu3/cpufreq/scaling_governor\n");


            System.out.println("TEST44!!!");
		    os.flush();
		    m_instream= new FileInputStream("/data/cpuinfo_cur_freq_buff.txt");
			if(m_instream!=null){
				m_inputreader=new InputStreamReader(m_instream);
				m_buffreader= new BufferedReader(m_inputreader);
				m_CurrFreq=new Integer(m_buffreader.readLine());  
			}   

		}
		catch(Exception ex){
			System.out.println(ex.getMessage());
		}
	}

/*read the current CPU frequency*/
	int CPU_FreqRead(){		
		try{
			os.writeBytes("cat /sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_cur_freq>/data/cpuinfo_cur_freq_buff.txt\n");
	        os.flush();
	        m_instream= new FileInputStream("/data/cpuinfo_cur_freq_buff.txt");
			m_inputreader=new InputStreamReader(m_instream);
			m_buffreader= new BufferedReader(m_inputreader);
			m_CurrFreq=new Integer(m_buffreader.readLine());  
		}
		catch(Exception ex){	
			m_CurrFreq=0;
			System.out.println(ex.getMessage());
		}
		return m_CurrFreq;
	}
	
	
/*change the frequency of CPU*/
void CPU_FreqChange(int freq_idx){
	
	if(freq_idx>=num_freq||freq_idx<0){
		System.out.println("freq_idx is wrong\n");
		return;
	}
		
	try{
        if(freq_idx==0){
            os.writeBytes("echo 800000 > /sys/devices/system/cpu/cpu0/cpufreq/scaling_setspeed\n");
            os.writeBytes("echo 800000 > /sys/devices/system/cpu/cpu1/cpufreq/scaling_setspeed\n");
            os.writeBytes("echo 800000 > /sys/devices/system/cpu/cpu2/cpufreq/scaling_setspeed\n");
            os.writeBytes("echo 800000 > /sys/devices/system/cpu/cpu3/cpufreq/scaling_setspeed\n");
        }
        else  if(freq_idx==1) {
            os.writeBytes("echo 1000000 > /sys/devices/system/cpu/cpu0/cpufreq/scaling_setspeed\n");
            os.writeBytes("echo 1000000 > /sys/devices/system/cpu/cpu1/cpufreq/scaling_setspeed\n");
            os.writeBytes("echo 1000000 > /sys/devices/system/cpu/cpu2/cpufreq/scaling_setspeed\n");
            os.writeBytes("echo 1000000 > /sys/devices/system/cpu/cpu3/cpufreq/scaling_setspeed\n");
        }
        else  if(freq_idx==2) {
            os.writeBytes("echo 1100000 > /sys/devices/system/cpu/cpu0/cpufreq/scaling_setspeed\n");
            os.writeBytes("echo 1100000 > /sys/devices/system/cpu/cpu1/cpufreq/scaling_setspeed\n");
            os.writeBytes("echo 1100000 > /sys/devices/system/cpu/cpu2/cpufreq/scaling_setspeed\n");
            os.writeBytes("echo 1100000 > /sys/devices/system/cpu/cpu3/cpufreq/scaling_setspeed\n");
        }
        else  if(freq_idx==3) {
            os.writeBytes("echo 1200000 > /sys/devices/system/cpu/cpu0/cpufreq/scaling_setspeed\n");
            os.writeBytes("echo 1200000 > /sys/devices/system/cpu/cpu1/cpufreq/scaling_setspeed\n");
            os.writeBytes("echo 1200000 > /sys/devices/system/cpu/cpu2/cpufreq/scaling_setspeed\n");
            os.writeBytes("echo 1200000 > /sys/devices/system/cpu/cpu3/cpufreq/scaling_setspeed\n");
        }
        else  if(freq_idx==4) {
            os.writeBytes("echo 1600000 > /sys/devices/system/cpu/cpu0/cpufreq/scaling_setspeed\n");
            os.writeBytes("echo 1600000 > /sys/devices/system/cpu/cpu1/cpufreq/scaling_setspeed\n");
            os.writeBytes("echo 1600000 > /sys/devices/system/cpu/cpu2/cpufreq/scaling_setspeed\n");
            os.writeBytes("echo 1600000 > /sys/devices/system/cpu/cpu3/cpufreq/scaling_setspeed\n");
        }
//        else  if(freq_idx==5) {
//            os.writeBytes("echo 600000 > /sys/devices/system/cpu/cpu0/cpufreq/scaling_setspeed\n");
//            os.writeBytes("echo 600000 > /sys/devices/system/cpu/cpu1/cpufreq/scaling_setspeed\n");
//            os.writeBytes("echo 600000 > /sys/devices/system/cpu/cpu2/cpufreq/scaling_setspeed\n");
//            os.writeBytes("echo 600000 > /sys/devices/system/cpu/cpu3/cpufreq/scaling_setspeed\n");
//        }
//        else  if(freq_idx==6) {
//            os.writeBytes("echo 800000 > /sys/devices/system/cpu/cpu0/cpufreq/scaling_setspeed\n");
//            os.writeBytes("echo 800000 > /sys/devices/system/cpu/cpu1/cpufreq/scaling_setspeed\n");
//            os.writeBytes("echo 800000 > /sys/devices/system/cpu/cpu2/cpufreq/scaling_setspeed\n");
//            os.writeBytes("echo 800000 > /sys/devices/system/cpu/cpu3/cpufreq/scaling_setspeed\n");
//        }
//        else  if(freq_idx==7) {
//            os.writeBytes("echo 900000 > /sys/devices/system/cpu/cpu0/cpufreq/scaling_setspeed\n");
//            os.writeBytes("echo 900000 > /sys/devices/system/cpu/cpu1/cpufreq/scaling_setspeed\n");
//            os.writeBytes("echo 900000 > /sys/devices/system/cpu/cpu2/cpufreq/scaling_setspeed\n");
//            os.writeBytes("echo 900000 > /sys/devices/system/cpu/cpu3/cpufreq/scaling_setspeed\n");
//        }
//        else  if(freq_idx==8) {
//            os.writeBytes("echo 1000000 > /sys/devices/system/cpu/cpu0/cpufreq/scaling_setspeed\n");
//            os.writeBytes("echo 1000000 > /sys/devices/system/cpu/cpu1/cpufreq/scaling_setspeed\n");
//            os.writeBytes("echo 1000000 > /sys/devices/system/cpu/cpu2/cpufreq/scaling_setspeed\n");
//            os.writeBytes("echo 1000000 > /sys/devices/system/cpu/cpu3/cpufreq/scaling_setspeed\n");
//        }
//        else  if(freq_idx==9) {
//            os.writeBytes("echo 1100000 > /sys/devices/system/cpu/cpu0/cpufreq/scaling_setspeed\n");
//            os.writeBytes("echo 1100000 > /sys/devices/system/cpu/cpu1/cpufreq/scaling_setspeed\n");
//            os.writeBytes("echo 1100000 > /sys/devices/system/cpu/cpu2/cpufreq/scaling_setspeed\n");
//            os.writeBytes("echo 1100000 > /sys/devices/system/cpu/cpu3/cpufreq/scaling_setspeed\n");
//        }
//        else  if(freq_idx==10) {
//            os.writeBytes("echo 1600000 > /sys/devices/system/cpu/cpu0/cpufreq/scaling_setspeed\n");
//            os.writeBytes("echo 1600000 > /sys/devices/system/cpu/cpu1/cpufreq/scaling_setspeed\n");
//            os.writeBytes("echo 1600000 > /sys/devices/system/cpu/cpu2/cpufreq/scaling_setspeed\n");
//            os.writeBytes("echo 1600000 > /sys/devices/system/cpu/cpu3/cpufreq/scaling_setspeed\n");
//        }
//        else  if(freq_idx==11) {
//            os.writeBytes("echo 1300000 > /sys/devices/system/cpu/cpu0/cpufreq/scaling_setspeed\n");
//            os.writeBytes("echo 1300000 > /sys/devices/system/cpu/cpu1/cpufreq/scaling_setspeed\n");
//            os.writeBytes("echo 1300000 > /sys/devices/system/cpu/cpu2/cpufreq/scaling_setspeed\n");
//            os.writeBytes("echo 1300000 > /sys/devices/system/cpu/cpu3/cpufreq/scaling_setspeed\n");
//        }
//        else  if(freq_idx==12) {
//            os.writeBytes("echo 1400000 > /sys/devices/system/cpu/cpu0/cpufreq/scaling_setspeed\n");
//            os.writeBytes("echo 1400000 > /sys/devices/system/cpu/cpu1/cpufreq/scaling_setspeed\n");
//            os.writeBytes("echo 1400000 > /sys/devices/system/cpu/cpu2/cpufreq/scaling_setspeed\n");
//            os.writeBytes("echo 1400000 > /sys/devices/system/cpu/cpu3/cpufreq/scaling_setspeed\n");
//        }
//        else  if(freq_idx==13) {
//            os.writeBytes("echo 1500000 > /sys/devices/system/cpu/cpu0/cpufreq/scaling_setspeed\n");
//            os.writeBytes("echo 1500000 > /sys/devices/system/cpu/cpu1/cpufreq/scaling_setspeed\n");
//            os.writeBytes("echo 1500000 > /sys/devices/system/cpu/cpu2/cpufreq/scaling_setspeed\n");
//            os.writeBytes("echo 1500000 > /sys/devices/system/cpu/cpu3/cpufreq/scaling_setspeed\n");
//        }
//        else  if(freq_idx==14) {
//            os.writeBytes("echo 1600000 > /sys/devices/system/cpu/cpu0/cpufreq/scaling_setspeed\n");
//            os.writeBytes("echo 1600000 > /sys/devices/system/cpu/cpu1/cpufreq/scaling_setspeed\n");
//            os.writeBytes("echo 1600000 > /sys/devices/system/cpu/cpu2/cpufreq/scaling_setspeed\n");
//            os.writeBytes("echo 1600000 > /sys/devices/system/cpu/cpu3/cpufreq/scaling_setspeed\n");
//        }
         os.flush();
	}
	catch(Exception ex){	

		System.out.println(ex.getMessage());
	}
	return ;
	
}

/*CPU  frequency manager*/
void CPU_FreqManager(float u_goal,float CPUUtil_Now){
	int freq_cur=CPU_FreqRead();
	if(freq_cur==0)freq_cur=freq_old;
	else freq_old=freq_cur;
	
	//System.out.println(freq_cur);

if(CPUUtil_Now>u_goal+0.1){
	if(freq_cur==CPU_FREQ0){
		CPU_FreqChange(1);
	}
	else if(freq_cur==CPU_FREQ1){
		CPU_FreqChange(2);
	}
	else if(freq_cur==CPU_FREQ2){
		CPU_FreqChange(3);
	}
	else if(freq_cur==CPU_FREQ3){
		CPU_FreqChange(4);
	}
	else if(freq_cur==CPU_FREQ4){
	}

}
else if(CPUUtil_Now<u_goal-0.1){
	if(freq_cur==CPU_FREQ0){
	}
	else if(freq_cur==CPU_FREQ1){
		CPU_FreqChange(0);
	}
	else if(freq_cur==CPU_FREQ2){
		CPU_FreqChange(1);
	}
	else if(freq_cur==CPU_FREQ3){
		CPU_FreqChange(2);
	}
	else if(freq_cur==CPU_FREQ4){
		CPU_FreqChange(3);
	}
}

//System.out.println(String.valueOf(freq_cur*CPUUtil_Now));
//System.out.println(String.valueOf(fu_goal));

return;
}	


}
