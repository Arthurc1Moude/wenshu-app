import { getApiUrl } from '../../utils/api';

interface AvatarProps {
  src?: string;
  username?: string;
  size?: 'sm' | 'md' | 'lg' | 'xl';
  isVip?: boolean;
  vipLevel?: number;
  className?: string;
}

const sizeMap = {
  sm: 'w-8 h-8 text-xs',
  md: 'w-10 h-10 text-sm',
  lg: 'w-14 h-14 text-base',
  xl: 'w-20 h-20 text-lg',
};

const sealSizeMap = {
  sm: 'w-3.5 h-3.5 text-[6px]',
  md: 'w-4 h-4 text-[7px]',
  lg: 'w-5 h-5 text-[8px]',
  xl: 'w-6 h-6 text-[10px]',
};

export default function Avatar({ src, username = '?', size = 'md', isVip, vipLevel, className = '' }: AvatarProps) {
  const initial = username ? username.charAt(0) : '?';
  const imgSrc = src ? getApiUrl(src) : null;
  return (
    <div className={`relative inline-block ${className}`}>
      <div className={`${sizeMap[size].split(' ')[0]} ${sizeMap[size].split(' ')[1]} overflow-hidden bg-paper border border-divider-heavy flex items-center justify-center`}
        style={{ borderRadius: '2px' }}>
        {imgSrc ? (
          <img src={imgSrc} alt={username} className="w-full h-full object-cover" />
        ) : (
          <span className="font-serif text-ink font-medium tracking-wide">{initial}</span>
        )}
      </div>
      {isVip && (
        <div className={`absolute -bottom-1 -right-1 ${sealSizeMap[size].split(' ')[0]} ${sealSizeMap[size].split(' ')[1]} border border-seal text-seal flex items-center justify-center bg-white font-serif font-bold`}
          style={{ borderRadius: '1px', transform: 'rotate(-5deg)' }}>
          <span className={sealSizeMap[size].split(' ')[2]}>印</span>
        </div>
      )}
      {!isVip && vipLevel && vipLevel > 0 && (
        <div className={`absolute -bottom-1 -right-1 ${sealSizeMap[size].split(' ')[0]} ${sealSizeMap[size].split(' ')[1]} bg-ink text-white flex items-center justify-center font-serif font-bold`}
          style={{ borderRadius: '1px' }}>
          <span className={sealSizeMap[size].split(' ')[2]}>{vipLevel}</span>
        </div>
      )}
    </div>
  );
}
