Name:           blog
Version:        1.0
Release:        1%{?dist}
Summary:        Simple Maven project
License:        BSD
URL:            https://github.com/nkonev/blog
BuildArch:      noarch

BuildRequires:  java-11-openjdk-devel

Requires:       java-11-openjdk-headless

%description
This is simple Maven project.

%build
git clone https://github.com/nkonev/blog.git
cd blog
pwd
ls -lah
./mvnw -Pfrontend clean package
pwd
ls -lah

%install
%mvn_install

%files -f .mfiles
