su - postgres -c 'psql -c "CREATE DATABASE vr;"'
su - postgres -c 'psql vr -c "CREATE USER vr;"'
su - postgres -c "psql vr -c \"alter user vr with encrypted password 'vr';\""
su - postgres -c "psql vr -c 'grant all privileges on database vr to vr;'"
