import React, { useState, useRef, useEffect } from 'react';
import { Button, ButtonProps } from './button';
import { cn } from '../../lib/utils';

interface DropdownItem {
  label: string;
  onClick: () => void;
  icon?: React.ElementType;
}

interface SimpleDropdownProps extends ButtonProps {
  label: React.ReactNode;
  items: DropdownItem[];
}

export const SimpleDropdown: React.FC<SimpleDropdownProps> = ({ label, items, className, ...props }) => {
  const [isOpen, setIsOpen] = useState(false);
  const ref = useRef<HTMLDivElement>(null);

  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (ref.current && !ref.current.contains(event.target as Node)) {
        setIsOpen(false);
      }
    };
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  return (
    <div className="relative inline-block text-left" ref={ref}>
      <Button onClick={() => setIsOpen(!isOpen)} className={className} {...props}>
        {label}
      </Button>
      {isOpen && (
        <div className="absolute right-0 z-50 mt-2 w-56 origin-top-right animate-in fade-in zoom-in-95 duration-100 rounded-md border border-border bg-popover shadow-lg ring-1 ring-black ring-opacity-5 focus:outline-none">
          <div className="py-1">
            {items.map((item, index) => (
              <button
                key={index}
                onClick={() => {
                  item.onClick();
                  setIsOpen(false);
                }}
                className="flex w-full items-center px-4 py-2 text-sm text-popover-foreground hover:bg-accent hover:text-accent-foreground"
              >
                {item.icon && <item.icon className="mr-2 h-4 w-4" />}
                {item.label}
              </button>
            ))}
          </div>
        </div>
      )}
    </div>
  );
};

