
int main(int argc, char* argv[])
{
	
	
	int status;
	int i=0;
	while(i<1000){
		join(exec("mfree_test2.coff",0,0), &status);
		i++;
		if(i%100==1) printf("%d\n",i);
	}
	
	exit(0); 
}