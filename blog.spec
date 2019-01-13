Name:           blog
Version:        1.0
Release:        1%{?dist}
Summary:        Simple Maven project
License:        BSD
URL:            https://github.com/nkonev/blog
Source0:        https://github.com/nkonev/blog.git
BuildArch:      noarch

BuildRequires:  maven-local
BuildRequires:  java-11-openjdk-devel

Requires:       java-11-openjdk-headless

%description
This is simple Maven project.

%package        javadoc
Summary:        Javadoc for %{name}

%description javadoc
This package contains the API documentation for %{name}.

%build
%mvn_build

%install
%mvn_install

%files -f .mfiles

%changelog
* Initial packaging