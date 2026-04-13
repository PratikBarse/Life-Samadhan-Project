import React from 'react';
import './AboutUs.css';

const AboutUs = () => {
  const team = [
    {
      name: 'Dipak Firake',
     
      image: '/assets/team/dipak.jpg'
    },
    {
      name: 'Ishan Raizada',
      
      image: '/assets/team/ishan.jpg'
    },
    {
      name: 'Pratik Barse',
      
      image: '/assets/team/pratik.jpg'
    },
    {
      name: 'Sashibhushan Mishra',
      
      image: '/assets/team/sashi.jpg'
    },
    {
      name: 'Suraj Rawat',
      
      image: '/assets/team/suraj.jpg'
    }
  ];

  return (
    <div className="about-container">
      <div className="about-hero">
        <h1>About LifeSamadhan</h1>
        <p>
          Empowering communities by connecting reliable service providers with customers in need. 
          We are dedicated to simplifying your daily life through technology.
        </p>
      </div>

      <div className="mission-section">
        <div className="mission-content">
          <h2>Our Mission</h2>
          <p>
            At LifeSamadhan, we believe in the dignity of labor and the power of connection. 
            Our mission is to create a seamless platform where skilled professionals can find meaningful work, 
            and households can access trusted help instantly.
          </p>
          <p>
            Whether it's a quick repair or a major project, we strive to ensure quality, transparency, 
            and safety in every interaction.
          </p>
        </div>
      </div>

      <div className="team-section">
        <h2>Meet Our Team</h2>
        <div className="team-grid">
          {team.map((member, index) => (
            <div className="team-card" key={index}>
              <div className="team-image-wrapper">
                <img 
                  src={member.image} 
                  alt={member.name} 
                  className="team-image"
                  onError={(e) => {
                    e.target.onerror = null; 
                    e.target.src = 'https://ui-avatars.com/api/?name=' + member.name.replace(' ', '+');
                  }}
                />
              </div>
              <div className="team-info">
                <h3>{member.name}</h3>
                <p>{member.role}</p>
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
};

export default AboutUs;
