import { Crown } from 'lucide-react';

interface AvatarProps {
  src?: string;
  username?: string;
  size?: 'sm' | 'md' | 'lg' | 'xl';
  isVip?: boolean;
  vipLevel?: number;
  className?: string;
}

const sizeMap = {
  sm: 'w-8 h-8',
  md: 'w-10 h-10',
  lg: 'w-16 h-16',
  xl: 'w-20 h-20',
};

export default function Avatar({ src, username = '?', size = 'md', isVip, vipLevel, className = '' }: AvatarProps) {
  return (
    <div className={`relative inline-block ${className}`}>
      <div className={`${sizeMap[size]} rounded-full overflow-hidden bg-gray-200 ring-2 ring-white`}>
        <img src={src || `https://ui-avatars.com/api/?name=${encodeURIComponent(username)}&background=000&color=fff&size=200&bold=true`} alt={username} className="w-full h-full object-cover" />
      </div>
      {isVip && (
        <div className="absolute -bottom-0.5 -right-0.5 bg-gold rounded-full w-4 h-4 flex items-center justify-center ring-2 ring-white">
          <Crown className="w-2.5 h-2.5 text-white" fill="white" />
        </div>
      )}
      {vipLevel && vipLevel > 0 && !isVip && (
        <div className="absolute -bottom-0.5 -right-0.5 bg-black text-white text-[8px] font-bold rounded-full w-4 h-4 flex items-center justify-center ring-2 ring-white">
          {vipLevel}
        </div>
      )}
    </div>
  );
}
