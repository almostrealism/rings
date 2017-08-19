import urllib2
import os
import pdb


# from python_helpers import instance
home = '/root'

# -----------boto
# print instance.get_tags_list()
os.system('mkdir '+home+'/.aws')
response = urllib2.urlopen('http://msched.us-west-2.elasticbeanstalk.com/credentials')
infile = response.read()
os.chdir(home+'/.aws')
with open('credentials', 'w') as outfile:
    outfile.write(infile)

# -----------
# Download the jars
# file.retrieve("")
# response = urllib2.urlopen('http://msched.us-west-2.elasticbeanstalk.com/')
# infile = response.read()
os.system('aws s3 cp s3://webservice-deploy/ModelSchedulerClient-0.1.jar '+home+'/ModelSchedulerClient-0.1.jar')

# -----------cron
# Download the heart_beat crontab
response = urllib2.urlopen('http://msched.us-west-2.elasticbeanstalk.com/crontab')
infile = response.read()
dir = '/etc/cron.d/'
os.chdir(dir)
with open('heart_beat', 'w') as outfile:
    outfile.write(infile)

# Start cron
os.system('/etc/init.d/cron start')
print 'cronned'

# -----------ssh
# Download the git credentials and add them to ssh-agent

if not os.path.exists(home + '/.ssh'):
    # This is sort of redundent as we assume ssh is installed in which case this dir exists
    os.system('mkdir ' + home + '/.ssh')

os.chdir(home + '/.ssh')

for p in ['prv', 'pub']:
    response = urllib2.urlopen('http://msched.us-west-2.elasticbeanstalk.com/'+p+'_git_key')
    infile = response.read()

    with open(p+'_git_key', 'w') as outfile:
        outfile.write(infile)

os.system('mv '+home+'/.ssh/pub_git_key '+home+'/.ssh/git_gen.pub')
os.system('mv '+home+'/.ssh/prv_git_key '+home+'/.ssh/git_gen')

# Add ssh keys

bash_cmd="""
eval `ssh-agent -s`;
chmod 400 """+home+"""/.ssh/git_gen;
ssh-add """+home+"""/.ssh/git_gen;
cat > """+home+"""/.ssh/config<<-EOM
Host bitbucket.org
 IdentityFile """+home+"""/.ssh/git_gen
"""
os.system(bash_cmd)
os.system('chmod 600 '+home+'/.ssh/config;')
# -----------git
# Sync repos
os.system('cd '+home)
os.system('git pull git@bitbucket.org:terraai/msched.git/')

# --------------- airflow.cfg
# Pull airflow.cfg
# afc = urllib.URLopener()
# afc.retrieve('http://msched.us-west-2.elasticbeanstalk.com/AirflowConfig', '~/airflow/airflow.cfg')
# os.system()
# --------------- TEMP
# move airflow.cfg to /airflow
# os.system('rm /airflow/airflow.cfg')


# AIRFLOW initdb
# os.system('airflow initdb')
# os.system('mv '+home+'/ModelSchedulerConfig/content/airflow.cfg /airflow/')


# Symbolic link to dags folder
os.system('ln -s '+home+'/dags /airflow/dags')
os.system('mkdir /airflow/logs')
os.system('airflow initdb')
