Name:           blog
Version:        1.0
Release:        1%{?dist}
Summary:        Simple Maven project
License:        BSD
URL:            https://github.com/nkonev/blog
Source0:        https://github.com/nkonev/blog.git
BuildArch:      noarch

BuildRequires:  java-11-openjdk-devel

Requires:       java-11-openjdk-headless

%description
This is simple Maven project.

%build
pwd
ls -lah
ls -lah ..
ls -lah ../..
ls -lah ../../..
./mvnw -Pfrontend clean package
pwd
ls -lah

%install
%mvn_install

%files -f .mfiles
