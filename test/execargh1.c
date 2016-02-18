/*
 * execargh1.c
 *
 * Print out the addresses of the arguments passed to exec to help
 * debug whether handleExec is reading the pointers correctly.  Note
 * that the child process does not access the arguments.
 */

#include "syscall.h"

int
main (int argc, char *argv[])
{
    int r;
    int childargc = 5;
    char *childargv[5] = {
	"write1.coff",
	"roses are red",
	"violets are blue",
	"I love Nachos",
	"and so do you",
    };

    printf ("childargc: %d\n", childargc);
    printf ("childargv: %d (0x%x)\n\n", childargv, childargv);

    // note that childargv == &childargv[0]
    printf ("&childargv[0]: %d (0x%x)\n", &childargv[0], &childargv[0]);
    printf ("&childargv[1]: %d (0x%x)\n", &childargv[1], &childargv[1]);
    printf ("&childargv[2]: %d (0x%x)\n", &childargv[2], &childargv[2]);
    printf ("&childargv[3]: %d (0x%x)\n", &childargv[3], &childargv[3]);
    printf ("&childargv[4]: %d (0x%x)\n", &childargv[4], &childargv[4]);
    printf ("childargv[0]: %d (0x%x)\n", childargv[0], childargv[0]);
    printf ("childargv[1]: %d (0x%x)\n", childargv[1], childargv[1]);
    printf ("childargv[2]: %d (0x%x)\n", childargv[2], childargv[2]);
    printf ("childargv[3]: %d (0x%x)\n", childargv[3], childargv[3]);
    printf ("childargv[4]: %d (0x%x)\n", childargv[4], childargv[4]);

       r = exec (childargv[0], childargc, childargv);
   join(r,0);
   
   if (r < 0) {
	printf ("exec returned an error: %d\n", r);
    } else {
	printf ("exec succeded, child pid %d\n", r); 
    }

    exit (r);
}
