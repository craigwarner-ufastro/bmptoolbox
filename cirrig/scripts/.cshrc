setenv BMPINSTALL /home/pi/bmpinstall
setenv UFMMTINSTALL /home/pi/bmpinstall
set autolist on

if (-e ${BMPINSTALL}/.ufcshrc) source ${BMPINSTALL}/.ufcshrc
set os = `/bin/uname`

alias ls 'ls --color=auto'
alias grep 'grep --color=auto'
alias fgrep 'fgrep --color=auto'
alias egrep 'egrep --color=auto'
