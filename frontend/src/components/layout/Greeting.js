import React, { useState, useEffect } from 'react';

const Greeting = ({ name }) => {
    const [greeting, setGreeting] = useState('');

    useEffect(() => {
        const updateGreeting = () => {
            const hour = new Date().getHours();
            if (hour < 12) {
                setGreeting('Good Morning');
            } else if (hour < 17) {
                setGreeting('Good Afternoon');
            } else {
                setGreeting('Good Evening');
            }
        };

        updateGreeting();
        // Update greeting every minute to stay accurate if left open
        const interval = setInterval(updateGreeting, 60000);
        return () => clearInterval(interval);
    }, []);

    // Helper to capitalize first letter of name
    const formatName = (str) => {
        if (!str) return '';
        return str.charAt(0).toUpperCase() + str.slice(1);
    };

    return (
        <span className="navbar-text text-dark fw-bold me-3">
            {greeting}, {formatName(name)}!
        </span>
    );
};

export default Greeting;
